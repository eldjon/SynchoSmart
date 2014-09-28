package tr.edu.metu.ii.aaa.core;


public abstract class Constants {
    
    // ******************************************************************************************************************* //
    // ************************************* IMPORTANT VARIABLE FOR TESTING ********************************************** //
    // ************************************* SET IT TO TRUE IN REAL APPLICATION ****************************************** //
    // ******************************************************************************************************************* //
	public static final boolean REALEASE_MODE = true;
	
    // ******************************************************************************************************************* //
    // ***************************************** ACTIVITY AND SERVICE INTENTS ******************************************** //
    // ******************************************************************************************************************* //
    public static final String INTENT_SPLASH_AC 
                                                = "tr.edu.metu.ii.aaa.activities.SplashAc"; 
    public static final String INTENT_SEAT_NUMBER_AC 
                                                = "tr.edu.metu.ii.aaa.activities.SeatNumberAc"; 
    public static final String INTENT_STATUS_AC 
                                                = "tr.edu.metu.ii.aaa.activities.StatusAc"; 
    public static final String INTENT_DATA_COLL_SERVICE 
                                                = "tr.edu.metu.ii.aaa.services.DataCollectorService";
    public static final String INTENT_CONNECTION_SERVICE 
                                                = "tr.edu.metu.ii.aaa.services.ServerComService"; 
    public static final String INTENT_MANAGER_SERVICE 
                                                = "tr.edu.metu.ii.aaa.services.ManagerService"; 
    public static final String INTENT_QUESTIONNAIRE_AC 
                                                = "tr.edu.metu.ii.aaa.activities.QuestionnaireAc"; 
    
    // ******************************************************************************************************************* //
    // ************************************************* PROBE NAMES ***************************************************** //
    // ******************************************************************************************************************* //
    public static final String PROBE_GYRO        = "edu.mit.media.funf.probe.builtin.GyroscopeSensorProbe";
    public static final String PROBE_ACC         = "edu.mit.media.funf.probe.builtin.AccelerometerSensorProbe";
    public static final String PROBE_GRAVITY     = "edu.mit.media.funf.probe.builtin.GravitySensorProbe";
    public static final String PROBE_LINEAR_ACC  = "edu.mit.media.funf.probe.builtin.LinearAccelerationSensorProbe";
    public static final String PROBE_ORIENTATION = "edu.mit.media.funf.probe.builtin.OrientationSensorProbe";
    public static final String PROBE_PROXIMITY   = "edu.mit.media.funf.probe.builtin.ProximitySensorProbe";
    public static final String PROBE_MAG_FIELD   = "edu.mit.media.funf.probe.builtin.MagneticFieldSensorProbe";
    public static final String PROBE_PRESSURE    = "edu.mit.media.funf.probe.builtin.PressureSensorProbe";
    public static final String PROBE_SOUND_LEVEL = "tr.edu.metu.ii.aaa.probes.SoundLevelProbe";

    // ******************************************************************************************************************* //
    // ************************************************* FORMATS ********************************************************* //
    // ******************************************************************************************************************* //
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    // ******************************************************************************************************************* //
    // ************************************************** EXTRAS ********************************************************* //
    // ******************************************************************************************************************* //
    public static final String EXTRA_QUESTIONNAIRE = "questionnaire";

    // ******************************************************************************************************************* //
    // ************************************************** PREFERENCES **************************************************** //
    // ******************************************************************************************************************* //
    public static final String PREF_LAST_SURVEY_ID          = "last_survey_id";
    public static final String PREF_SEAT_PROVIDED           = "seat_provided";
    public static final String PREF_HAS_QUESTIONNAIRE       = "has_questionnaire";
    public static final String PREF_QUESTIONNAIRE_SUBMITTED = "questionnaire_provided";
    public static final String PREF_QUESTIONNAIRE           = "questionnaire";
    public static final String PREF_APP_CRASHED             = "app_crashed";
    
}
