package tr.edu.metu.thesis.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.resource.spi.IllegalStateException;

import tr.edu.metu.thesis.beans.utils.InstanceStatus;
import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.events.EventDispatcher;
import tr.edu.metu.thesis.events.ServerEvent;
import tr.edu.metu.thesis.events.ServerEventListener;
import tr.edu.metu.thesis.events.SoapEvent;
import tr.edu.metu.thesis.events.SocketEvent;
import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.jpa.Participant.DeviceStatus;
import tr.edu.metu.thesis.socket.ServerMessage.SocketMessageType;


public class SocketHandler implements ServerEventListener{

	public static final int      DEFAULT_PORT            = 24564;
	public static final String   LOCAL_IP_DIFF           = "192.168";
	public static final String   LOCAL_IP_DIFF_EXTRA     = "144.122";
	
	protected static final int   WORKER_THREAD_IDLE_TIME  = 60000; // 30 seconds
	protected static final int   WORKER_THREAD_SLEEP_TIME = 2000;  // 2 seconds
	
	public static final int      STATE_DESTROYED         = 1;
	public static final int      STATE_CREATED           = 2;
	public static final int      STATE_CONNECTING        = 3;
	public static final int      STATE_DEVICES_CONNECTED = 4;
	public static final int      STATE_READING           = 5;
	public static final int      STATE_WRITING           = 6;
	private static SocketHandler _instance;

	//network address for server socket
	private InetSocketAddress                   _adr;
	private int                                 _port;
	private Selector                            _selector;
	
	private Thread                              _connThread;
	private Thread                              _workerThread;
	private HashMap<Device, SocketChannel>      _deviceSocketList;
	private ServerSocketChannel                 _serverSChannel;
	
	private Set<Device>         				_toBeStarted;// devices which need to be restarted
	private Set<Device>        					_toBeDisconnected;
	private CopyOnWriteArrayList<SocketMessage> _receivedMsgs;
	private CopyOnWriteArrayList<SocketMessage> _toSendMsgs;

	private int                                 _state         = 0;
	private long                                _startConnTime = 0;
	private int                                 _flags         = 0;
	
	
	private SocketHandler() {
		
		init();
	}
	
	public static SocketHandler getInstance(){
		
		synchronized (SocketHandler.class) {
			if(_instance == null)
				_instance = new SocketHandler();
		}
		
		return _instance;
	}
	
	protected void init(){
		
		System.out.println(this.getClass().getName() + " : Initializing SocketHandler!!");
		setState(STATE_CREATED);
		String localIp = findLocalIp();
		System.out.println(this.getClass().getName() + " : Server ip is : " + localIp);
		_port = DEFAULT_PORT;
		// create a selector which will manage our single ServerSocketChannel
		try {
			_selector         = SelectorProvider.provider().openSelector();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			_adr = new InetSocketAddress(InetAddress.getByName(localIp), 
										_port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			_serverSChannel = ServerSocketChannel.open();
		} catch (IOException e) {
			
			System.out.println("=================================== why address is already in use ============================ " + _serverSChannel.isOpen());
			e.printStackTrace();
		}
		try {
			_serverSChannel.socket().bind(_adr);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				_port++;
				_adr = new InetSocketAddress(InetAddress.getByName(localIp), 
					  						_port);
			} catch (UnknownHostException e1) {
			}
		}
		try {
			_serverSChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// register our socket channel to the selector
		// for connections/read/write operations
		try {
			_serverSChannel.register(_selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
		
		_deviceSocketList = new HashMap<>();
		_toSendMsgs       = new CopyOnWriteArrayList<>();
		_receivedMsgs     = new CopyOnWriteArrayList<>();
		_toBeDisconnected = new HashSet<>();
		_toBeStarted      = new HashSet<>();
	}
	
	// ******************************************************************************* //
	// ************************** METHODS CALLED BY JAVA BEANS *********************** //
	// ******************************************************************************* //
	public synchronized void open(List<Device> devices) throws IllegalStateException{
		
		if(_state > STATE_CREATED)
			throw new IllegalStateException("The SocketHandler is already started! "
					   					  + "You should stop the instance first !");
		setState(STATE_CREATED);
		_connThread   = new Thread(new SocketRunner());
		_workerThread = new Thread(new WorkerRunner());
		
		if(!_connThread.isAlive())
			_connThread.start();
		
		if(!_workerThread.isAlive())
			_workerThread.start();

		for(Device d : devices)
			_deviceSocketList.put(d, null);
		// register this SocketHandler for receiving events
		EventDispatcher.getInstance().registerListener(_instance);

		// fire an event notifying the start of SocketHandler
		SocketEvent event = new SocketEvent(this, 
											SocketEvent.EVENT_SOCKET_OPENED);
		EventDispatcher.getInstance().fireEvent(event);
		setState(STATE_CONNECTING);
	}
	
	public void sendMessage(SocketMessage msg) throws IllegalStateException{
		
		synchronized (this) {
			
			if(_state > STATE_DEVICES_CONNECTED)
				throw new IllegalStateException("The SocketHandler is already started! "
						   					  + "You should stop the instance first !" + "STATE = " + _state);
			if(_state == STATE_DESTROYED)
				throw new IllegalStateException("The SocketHandler is destroyed! "
						   					  + "Restart the socket first!" + "STATE = " + _state);
		}

		_toSendMsgs.clear();
		_toSendMsgs.add(msg);
	}
	
	public void startDevice(Device dev){

		synchronized (_toBeDisconnected) {
			_toBeDisconnected.remove(dev);
		}
		
		synchronized (_toBeStarted) {
			_toBeStarted.add(dev);
		}
	}
	
	public void stopDevice(Device dev){
		
		if(_state <= STATE_CREATED)
			return;
		
		synchronized (_toBeStarted) {
			_toBeStarted.remove(dev);
		}
		
		synchronized (_toBeDisconnected) {
			_toBeDisconnected.add(dev);
		}
	}
	
	public void receiveMessage(SocketMessage msg) throws IllegalStateException{
		
		synchronized (this) {
			
			if(_state > STATE_CREATED)
				throw new IllegalStateException("The SocketHandler is already started! "
						   					  + "You should stop the instance first !");
		}

		_receivedMsgs.add(msg);
	}
	
	/**
	 * Close the connection if there is any open.
	 * @return The amount of time in milliseconds during which the connection
	 * has been opened.
	 * @throws IllegalStateException if trying to close an already closed 
	 * connection.
	 */
	public synchronized long close() throws IllegalStateException{
		
		if(_state <= STATE_CREATED)
			throw new IllegalStateException("The SocketHandler is not started! ");
		closeSockets();
		if(_connThread.isAlive())
			_connThread.interrupt();
		_connThread     = null;
		if(_workerThread.isAlive())
			_workerThread.interrupt();
		_workerThread   = null;
		_serverSChannel = null;
		_toSendMsgs.clear();
		_toSendMsgs     = null;
		_receivedMsgs.clear();
		_receivedMsgs   = null;
		_state          = 0;
		_adr            = null;
		System.out.println(SocketHandler.class.getName() + " : Stoping SocketHandler!!");
		// register this SocketHandler for receiving events
		EventDispatcher.getInstance().unregisterListener(_instance);
		_instance       = null;

		// fire an event notifying the end of this SocketHandler
		SocketEvent event = new SocketEvent(this, 
											SocketEvent.EVENT_SOCKET_CLOSED);
		EventDispatcher.getInstance().fireEvent(event);
		return (System.currentTimeMillis() - _startConnTime);
	}
	
	public long getStartTime(){
		
		return _startConnTime;
	}
	
	public boolean areAllDevicesConnected(){
		
		if(_deviceSocketList.size() <= 0)
			return false;
		
		for(Device d : _deviceSocketList.keySet())
			if(!d.getStatus().equals(InstanceStatus.CONNECTED))
				return false;
		
		return true;
	}
	
	public boolean isDeviceConnected(String imei){
		
		for(Device d : _deviceSocketList.keySet())
			if(d.getImei().equals(imei))
				if(d.getStatus().equals(InstanceStatus.CONNECTED.name()))
					return true;
		
		return false;
	}
	
	public boolean isDeviceConnected(Device device){
		
		return this.isDeviceConnected(device.getImei());
	}

	public Set<Device> getActiveDevices(){
		
		return new HashSet<Device>(_deviceSocketList.keySet());
	}
	
	public boolean isAnyDeviceConnected(){
		
		for(Device d : _deviceSocketList.keySet())
			if(isDeviceConnected(d))
				return true;
		
		return false;
	}
	
	public void removeDevice(Device device) throws IllegalStateException{
		
		if(_state <= STATE_CREATED)
			throw new IllegalStateException("The SocketHandler had not been started yet! "
					   					  + "Removing a device on unstarted socket has no effect!");
		
		if(isDeviceConnected(device))
			throw new IllegalStateException("You cannot remove a connected device! ");

		_toBeDisconnected.add(device);
		// stop the device if neccessary and remove it from the list
		synchronized (_deviceSocketList) {
		
			SocketChannel sc = _deviceSocketList.get(device);
			if(sc != null){
				if(sc.isConnected())
					try {
						sc.socket().close();
						SocketEvent event = new SocketEvent(this,
															SocketEvent.EVENT_CLIENT_DISCONNECTED);
						event.setDevice(device);
						event.setSocketChannel(sc);
						EventDispatcher.getInstance().fireEvent(event);
						System.out.println("SocketHandler.removeDevice() : " + device.getImei());
						_deviceSocketList.remove(device);
					} catch (IOException e) {
						e.printStackTrace();
					}
			} else {
				System.out.println("SocketHandler.removeDevice() : " + device.getImei());
				_deviceSocketList.remove(device);
			}
		}
	}
	
	/**
	 * The SocketHandler is active if at least one device is trying to 
	 * connect or is already connected.
	 * @return
	 */
	public synchronized boolean isActive(){
		
		if(_state >= STATE_CONNECTING)
			return true;
		
		return false;
	}
	
	public int getPort(){
		
		if(_port > 0)
			return _port;
		
		return DEFAULT_PORT;
	}
	// ******************************************************************************* //
	// ******************************* MANAGE FLAGS ********************************** //
	// ******************************************************************************* //
    public void setFlag(int flag) { 
        
        _flags |= flag;
    }
    
    public void resetFlag(int flag) { 
        
        _flags &= ~flag;
    }
    
    public boolean isFlagOn(int flag) { 
        
        return (_flags & flag) == 0 ? false : true;
    }
	
	// ******************************************************************************* //
	// **************************** PROTECTED METHODS ******************************** //
	// ******************************************************************************* //
	protected synchronized void setState(int state){
		
		if(_state == STATE_DESTROYED)
			return;
		_state = state;
	}

	protected void readFromClient(){
		
		synchronized (_receivedMsgs) {
			
//			for(SocketListener sl : _listeners)
//				sl.onMsgSent(msg);
		}
	}
	
	protected void writeToClient(){
		
		synchronized (_toSendMsgs) {
			
			Iterator<SocketMessage> msgIt = _toSendMsgs.iterator();
			while(msgIt.hasNext()){
				
				SocketMessage msg = msgIt.next();
				Iterator<SocketChannel> iterator = _deviceSocketList.values()
																    .iterator();
				Iterator<Device> devIt = _deviceSocketList.keySet()
					    								  .iterator();
				while(iterator.hasNext()){
					
					SocketChannel sc  = null;
					Device        dev = null;
					try {
						sc  = iterator.next();
						dev = devIt.next();
						if(sc != null){
							
							System.out.println(SocketHandler.this.getClass().getName() 
								   	+ ": Writing data to a SocketChannel: " 
								   	+ sc.getRemoteAddress().toString());
							byte[] bytes = msg.getMsgInBytes();
							sc.write(ByteBuffer.wrap(bytes));
						}
					} catch (IOException e) {
						
						// if we tried to write but the device is disconnected we need 
						// to update the device status and call listeners
						System.out.println(" ============================ why we received a broken pipe exception ==================================== ");
						e.printStackTrace();
						if(dev != null){
							dev.setStatus(DeviceStatus.DISCONNECTED.name());
							SocketEvent event = new SocketEvent(this, 
																SocketEvent.EVENT_CLIENT_DISCONNECTED);
							event.setDevice(dev);
							event.setSocketChannel(sc);
							EventDispatcher.getInstance().fireEvent(event);
						}
					}
				}
				if(msg.getType()._msg == SocketMessageType.STOP._msg){
					System.out.println(SocketHandler.this.getClass().getName() +": Received a STOP message!" );
					System.out.println(SocketHandler.this.getClass().getName() +": SocketHandler should stop its work now !" );
					setState(STATE_DESTROYED);
				}
				
				// call listeners of Writing
				SocketEvent event = new SocketEvent(this, 
													SocketEvent.EVENT_MSG_SENT, 
													msg);
				EventDispatcher.getInstance().fireEvent(event);
			}
			_toSendMsgs.clear();
		}
	}
	
	protected SelectionKey connectClient(){
		
		SocketChannel conClient;
		try {
			
			System.out.println(SocketHandler.class.getName() + " : Waiting for clients to connect !!");
			conClient        = _serverSChannel.accept();
			SelectionKey key = addClientSChannel(conClient);
			if(key == null){
				
				conClient.close();
				return null;
			}
			
			return key;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	protected void disconnectClient(final SelectionKey key){
		
		Device dev = (Device) key.attachment();
		try {
			key.channel().close();
			dev.setStatus(DeviceStatus.DISCONNECTED.name());
			key.attach(dev);
			System.out.println(SocketHandler.class.getName() + " : Device disconnected IMEI = " + dev.getImei());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected SelectionKey addClientSChannel(final SocketChannel client){
		
		InetSocketAddress clientAdr;
		try {
			clientAdr = (InetSocketAddress) client.getRemoteAddress();
			Device d  = findDeviceByIp(clientAdr.getHostName());

			// if we found the connected device
			if(d == null){
			
				System.out.println(SocketHandler.class.getName() 
								   + " : The device with given IP address: " 
								   + client.getRemoteAddress()
								   + " is not selected for the active survey!!");
				return null;
			}else{
				
				client.configureBlocking(false);
				SelectionKey devKey  = client.register(_selector, SelectionKey.OP_WRITE);
				_deviceSocketList.put(d, client);
				
				if(_deviceSocketList.get(d) == null){
					
					System.out.println(SocketHandler.class.getName() 
							   + " : The device identified as : " 
							   + d.getImei()
							   + " is not selected for the active survey!!");
					return null;
				}

				System.out.println(SocketHandler.class.getName() 
								   + " : Selected device is connected : " 
								   + d.getImei());
				devKey.attach(d); //attach the Device object to this key
				// update the status of the Device in database
				d.setStatus(DeviceStatus.CONNECTED.name());
				return devKey;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected synchronized boolean shouldStop(){
		
		if(_state == STATE_DESTROYED)
			return true;
		return false;
	}	
	
	protected String findLocalIp(){
		
		try {
			Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			while(networks.hasMoreElements()){
				
				NetworkInterface ni = networks.nextElement();
				Enumeration<InetAddress> adr = ni.getInetAddresses();
				while(adr.hasMoreElements()){
					
					InetAddress ipAdress = adr.nextElement();
					if(ipAdress.getHostAddress().contains(LOCAL_IP_DIFF) ||
							ipAdress.getHostAddress().contains(LOCAL_IP_DIFF_EXTRA))
						return ipAdress.getHostAddress();
				}
			}
		} catch (SocketException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	protected Device findDeviceByIp(String ip){
		
		Set<Device> devices = _deviceSocketList.keySet();
		for(Device d : devices)
			if((d.getCurrentIpAddress() != null) 
					&& (d.getCurrentIpAddress().equals(ip)))
				return d;
		
		return null;
	}
	
	protected synchronized void closeSockets(){
		
		try {
			if(_serverSChannel != null){
				
				System.out.println(SocketHandler.this.getClass().getName() 
								   + ":  ------------------------- CLOSING SOCKET -------------------------- ");
				_serverSChannel.close();
				if(_selector != null)
					_selector.close();
			}
			for(SocketChannel sc : _deviceSocketList.values())
				if((sc != null) && sc.isConnected())
					sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		_deviceSocketList.clear();
		_deviceSocketList = null;
		_adr              = null;
	}
	
	protected void clearResources(){
		
		_serverSChannel = null;
		_toSendMsgs.clear();
		_toSendMsgs     = null;
		_receivedMsgs.clear();
		_receivedMsgs   = null;
		// register this SocketHandler for receiving events
		EventDispatcher.getInstance().unregisterListener(_instance);
		_instance       = null;
		_state          = 0;
	}
	
	// ******************************************************************************* //
	// ********************* WORKER THREAD IMPLEMENTATION **************************** //
	// ******************************************************************************* //
	protected class SocketRunner implements Runnable{

		@Override
		public void run() {
			
			System.out.println(SocketHandler.class.getName() + " : Starting SocketHandler!!");
			
			// main while loop. Assuring that the thread will keep working
			while(true){
				
				synchronized (SocketHandler.class) {
					
					// If we received a stop CMD clear all resources and stop all THREADS
					if(shouldStop()){
						System.out.println(SocketHandler.this.getClass().getName() +": SOCKET_THREAD WILL TERMINATE!" );
						closeSockets();
						if(_workerThread.isAlive())
							_workerThread.interrupt();
						_workerThread   = null;
						clearResources();
						// fire an event notifying the end of this SocketHandler
						SocketEvent event = new SocketEvent(this, 
															SocketEvent.EVENT_SOCKET_CLOSED);
						EventDispatcher.getInstance().fireEvent(event);
						return;
					}
				}
				
				// select our socket channel whenever it gets ready.
				try {
					_selector.select();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
				// we iterate through all SelectionKeys registered in our selector 
				// for that SelectionKey we wait for devices to connect.
				Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();
				while(keys.hasNext()){
					SelectionKey key = keys.next();

					if(key.isAcceptable()){
						
						System.out.println(SocketHandler.class.getName() + " : Ready receive a connection ! ");
						setState(STATE_CONNECTING);
						_startConnTime = System.currentTimeMillis();
						SelectionKey clientKey = connectClient();
						
						if(clientKey != null){
							
							// fire client connected event
							SocketEvent event = new SocketEvent(this, 
																SocketEvent.EVENT_CLIENT_CONNECTED);
							event.setDevice((Device)clientKey.attachment());
							event.setSocketChannel((SocketChannel)clientKey.channel());
							EventDispatcher.getInstance().fireEvent(event);
						} else{
							
							SocketEvent event = new SocketEvent(this, 
																SocketEvent.EVENT_CLIENT_DISCONNECTED);
							EventDispatcher.getInstance().fireEvent(event);
						}
						setState(STATE_DEVICES_CONNECTED);
					}
					if(key.isWritable()){
						
						setState(STATE_WRITING);
						writeToClient();
						setState(STATE_DEVICES_CONNECTED);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							System.out.println("SOCKET_THREAD caught InterruptionException and will TERMINATE!"); 
							return;
						}
					}
					if(key.isReadable()){
						
						setState(STATE_READING);
						readFromClient();
						setState(STATE_DEVICES_CONNECTED);
					}
					if(key.isConnectable()){
						disconnectClient(key);
						if(key.attachment() != null){
							
							// call listeners of Writing
							SocketEvent event = new SocketEvent(this, 
																SocketEvent.EVENT_CLIENT_DISCONNECTED);
							event.setDevice((Device)key.attachment());
							event.setSocketChannel((SocketChannel)key.channel());
							EventDispatcher.getInstance().fireEvent(event);
						}
					}
					keys.remove();
				}
			}// end of Main WHILE loop
		}
	}

	protected class WorkerRunner implements Runnable{

		// send an empty msg to clients periodically in order to check if they are still connected
		private long _checkTimer = 0;
		@Override
		public void run() {
			
			while(true){
				
				synchronized (SocketHandler.class) {
					
					// If we received a stop CMD clear all resources and stop all THREADS
					if(shouldStop()){
						System.out.println(SocketHandler.this.getClass().getName() +": WORKER_THREAD WILL TERMINATE!" );
						closeSockets();
						if(_connThread.isAlive())
							_connThread.interrupt();
						_connThread     = null;
						clearResources();
						// fire an event notifying the end of this SocketHandler
						SocketEvent event = new SocketEvent(this, 
															SocketEvent.EVENT_SOCKET_CLOSED);
						EventDispatcher.getInstance().fireEvent(event);
						return;
					}
				}
				
				// if we received a STOP message and we have no device connected to 
				// the socket, then we need to end the work of SocketHandler
				synchronized (_toSendMsgs) {
					if(_toSendMsgs.size() > 0){
						for(SocketMessage msg : _toSendMsgs){
							if(msg.getType()._msg == SocketMessageType.STOP._msg){
								if(!isAnyDeviceConnected()){
									
									System.out.println(SocketHandler.this.getClass().getName() +": We received a stop message "
											   + "and we dont have any device connected !" );
									setState(STATE_DESTROYED);
								}
							}
						}
					}
				}
				
				// if we received sth to process we have to wake up the SocketThread
				if(_toSendMsgs.size() > 0 || _receivedMsgs.size() > 0 || _toBeStarted.size() > 0){
					_selector.wakeup();
				}
				// if a device requested to be disconnected, 
				// we will disconnect the socket and update the state of the device
				synchronized (_toBeDisconnected) {
					if(_toBeDisconnected.size() > 0){
						for(Device d : _toBeDisconnected){
							
							d.setStatus(DeviceStatus.DISCONNECTED.name());
							synchronized (_deviceSocketList) {
								
								SocketChannel sc = _deviceSocketList.get(d);
								if(sc != null && sc.isConnected())
									try {
										sc.close();
									} catch (IOException e) {
										e.printStackTrace();
									}finally{
										// call listeners of Writing
										SocketEvent event = new SocketEvent(this, 
																			SocketEvent.EVENT_CLIENT_DISCONNECTED);
										event.setDevice(d);
										event.setSocketChannel(sc);
										EventDispatcher.getInstance().fireEvent(event);
									}
							}
						}
					}
				}
				
				synchronized (_toBeStarted) {
					// send a request to the given device to start
					if(_toBeStarted.size() > 0){
						
					}
				}
				// we check periodically if devices are still connected to the server
				// by sending an EMPTY message to all devices.
				if((System.currentTimeMillis() - _checkTimer) > WORKER_THREAD_IDLE_TIME){
					
					_checkTimer = System.currentTimeMillis();
					ServerMessage msg = new ServerMessage(SocketMessageType.EMPTY);
					try {
						sendMessage(msg);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(WORKER_THREAD_SLEEP_TIME);
				} catch (InterruptedException e) {
					
					System.out.println("WORKER_THREAD cought InterruptionException and will TERMINATE!"); 
					return;
				}
			}
		}
	}

	// ******************************************************************************* //
	// ********************************** HANDLE EVENTS PART ************************* //
	// ******************************************************************************* //
	@Override
	public void onServerEvent(ServerEvent event) {
		
		if(event instanceof SoapEvent){
			
			SoapEvent soapE = (SoapEvent) event;
			// if the getSocket method was called and the socket provided to that device
			// is appropriate then we need to update the IP of the given device
			if(soapE.getMethodCalled().equals("getSocket") 
					&& (int)soapE.getResult() > 0){
				List<Device> devs = EAOManager.findByField(Device.class, 
														   "imei", 
														   soapE.getCallingImei());
				if(devs.size() > 0){
					Set<Device> devices = _deviceSocketList.keySet();
					for(Device d : devices)
						if(d.equals(devs.get(0)))
							d.setCurrentIpAddress(devs.get(0).getCurrentIpAddress());
				}
			}
		}
	}
	
}
