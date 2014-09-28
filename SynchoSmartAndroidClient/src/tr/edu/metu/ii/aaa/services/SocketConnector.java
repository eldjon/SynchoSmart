package tr.edu.metu.ii.aaa.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.BaseAnalysisObject;
import tr.edu.metu.ii.aaa.events.SocketEvent;
import tr.edu.metu.ii.aaa.messages.ServerMessage;
import tr.edu.metu.ii.aaa.utils.ThreadUtils;


public class SocketConnector extends BaseAnalysisObject{

    protected static final int STATUS_CREATED   = 1;
    protected static final int STATUS_CONNECTED = 2;
    protected static final int STATUS_RECEIVING = 3;
    protected static final int STATUS_CLOSED    = 4;
    // wait three minutes for a message from server.
    // if there is no message during this period of time close the socket
    protected static final int MAX_WAIT_TIME    = 180000;
    
    protected SocketChannel        _serverSChannel;
    protected Selector             _selector;
    protected InetSocketAddress    _serverAdr;
    
    protected AnalysisApp          _app;
    protected Thread               _socketThread;
    private int                    _status = STATUS_CLOSED;
    
    
    public SocketConnector(AnalysisApp app){
        
        super();
        _app = app;
    }
    
    public void start(){
        
        synchronized (this) {
            
            if(_status == STATUS_CONNECTED || _status == STATUS_RECEIVING)
                return;
        }
    	System.out.println(getClass().getName() + ": Socket Thread started (status will set to CREATED)!");
        setStatus(STATUS_CREATED);
        _socketThread = new Thread(new SocketRunnable(), "SOCKET_THREAD");
        _socketThread.start();
        SocketEvent event = new SocketEvent(this, 
                                            System.currentTimeMillis(), 
                                            SocketEvent.SOCKET_START);
        fireEvent(event);
    }
    
    public synchronized void stop(){
        
        synchronized (this) {
            if(_status == STATUS_CREATED)
                return;
        }
        
    	System.out.println(getClass().getName() + ": Socket Thread stopped (from stop command)!");
        if(_socketThread != null){
            
            _socketThread.interrupt();
            _socketThread = null;
        }
        _app.setServerSocket(-1);
        
        try {
            if(_serverSChannel.isConnected()){
            	
                _serverSChannel.close();
                _selector.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SocketEvent event = new SocketEvent(this, 
                                            System.currentTimeMillis(), 
                                            SocketEvent.SOCKET_STOP);
        fireEvent(event);
        setStatus(STATUS_CLOSED);
    }
    
    public synchronized boolean isAlive(){
        
        if((_status != STATUS_CLOSED) 
                        && (_status != STATUS_CREATED))
            return true;
        
        return false;
    }
    
    protected synchronized void setStatus(int status){
        
        _status = status;
    }
    
    protected class SocketRunnable implements Runnable {

        @Override
        public void run() {

            try {
                
                while(true){
                    
                    if(_status == STATUS_CLOSED)
                        return;

                    if(_app.getServerSocket() > 0)
                        break;
                    ThreadUtils.sleep(5000);
                }
                
                try {
                    _selector = SelectorProvider.provider().openSelector();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // connect to the server
                System.out.println("The Server address is : " 
                                   + _app.getServerIp() 
                                   + ":" + _app.getServerSocket());
                _serverAdr = new InetSocketAddress(_app.getServerIp(), 
                                                   _app.getServerSocket());
                
                _serverSChannel = SocketChannel.open();
                _serverSChannel.configureBlocking(false);
                _serverSChannel.register(_selector, SelectionKey.OP_READ);
                _serverSChannel.connect(_serverAdr);
                
                // wait for the client to connect to the server
                while(!_serverSChannel.finishConnect()){
                    
                	System.out.println("SocketConnector.SocketRunnable.run()");
                    if(_status == STATUS_CLOSED)
                        return;

                    ThreadUtils.sleep(1000);
                }
                // if the client is connected successfuly update the state
                setStatus(STATUS_CONNECTED);
                SocketEvent event = new SocketEvent(this, 
                                                    System.currentTimeMillis(), 
                                                    SocketEvent.SOCKET_CONNECT);
                fireEvent(event);
                // start observing the socket for any bytes coming in
                while(true){
                    if(_status == STATUS_CLOSED)
                        return;
                    
                    if(_selector != null 
                                    && _selector.isOpen() 
                                    && _selector.select(1000) > 0){
                        if(_status == STATUS_CLOSED)
                            return;
                        
                        synchronized (_selector) {
							
                            Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();
                            while(keys.hasNext()){
                                SelectionKey sk = keys.next();
                                if(sk.isValid()){
                                    
                                    if(sk.isConnectable()){
                                        // connect socket
                                    }else if(sk.isReadable()){
                                        // read data from socket
                                        readSocketMsg();
                                    }else if (sk.isWritable()){
                                        // write data to socket
                                    }
                                }
                                keys.remove();
                            }
						}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                forceClose();
            }
        };
        
    }
    
    private void readSocketMsg(){
        
        setStatus(STATUS_RECEIVING);
        ByteBuffer data = ByteBuffer.allocate(ServerMessage.MESSAGE_SIZE);
        
        try {
            while( _serverSChannel.isConnected() && ((_serverSChannel.read(data)) > 0)){
                
                byte[] byteData   = data.array();
                data.flip();
                int msgType       = data.get();
                ServerMessage msg = new ServerMessage(byteData);
                SocketEvent event = null;
                
                switch (msgType) {
                    case 1: //message start 4
                        System.out.println(SocketConnector.this.getClass().getName() 
                                           + ": Received Start CMD from server ! ");
                        event = new SocketEvent(SocketConnector.this, 
                                                System.currentTimeMillis(), 
                                                SocketEvent.MSG_START);
                        event.setMsg(msg);
                        fireEvent(event);
                        break;
                    
                    case 2: //message stop
                        
                        System.out.println(SocketConnector.this.getClass().getName() 
                                           + ": Received Stop CMD from server ! ");
                        event = new SocketEvent(SocketConnector.this, 
                                                System.currentTimeMillis(), 
                                                SocketEvent.MSG_STOP);
                        event.setMsg(msg);
                        fireEvent(event);
                        break;
                    
                    case 3: //message pause
                        
                        System.out.println(SocketConnector.this.getClass().getName() 
                                           + ": Received Pause CMD from  server ! ");
                        event = new SocketEvent(SocketConnector.this, 
                                                System.currentTimeMillis(), 
                                                SocketEvent.MSG_PAUSE);
                        event.setMsg(msg);
                        fireEvent(event);
                        break;
                    
                    case 4: //message resume
                        
                        System.out.println(SocketConnector.this.getClass().getName() 
                                           + ": Received Resume CMD from server ! ");
                        event = new SocketEvent(SocketConnector.this, 
                                                System.currentTimeMillis(), 
                                                SocketEvent.MSG_RESUME);
                        event.setMsg(msg);
                        fireEvent(event);
                        break;
                    
                    case -1: //message empty (just checking if the device is still connected)
                        
                        System.out.println(SocketConnector.this.getClass().getName() 
                                           + ": Received Empty CMD from server ! ");
                        event = new SocketEvent(SocketConnector.this, 
                                                System.currentTimeMillis(), 
                                                SocketEvent.MSG_EMPTY);
                        event.setMsg(msg);
                        fireEvent(event);
                        break;
                    
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            forceClose();
        } finally {
            setStatus(STATUS_CONNECTED);
        }
    }
    
    protected void forceClose(){
    	
        setStatus(STATUS_CLOSED);
        try {
            _serverSChannel.close();
            _selector.close();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
        SocketEvent event = new SocketEvent(this, 
                                            System.currentTimeMillis(), 
                                            SocketEvent.SOCKET_STOP);
        fireEvent(event);
    }
}
