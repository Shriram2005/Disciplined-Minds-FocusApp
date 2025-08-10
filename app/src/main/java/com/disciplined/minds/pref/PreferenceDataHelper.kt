
import android.content.Context
import com.disciplined.minds.pref.SharedPreferenceHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Square Infosoft.
 */

class PreferenceDataHelper private constructor(context: Context) {

    private val mSharedPreferenceHelper: SharedPreferenceHelper




    companion object {

        private const val APP_LIST = "app_list"
        private const val IS_STUDY_MODE = "is_study_mode"
        private const val IS_FOCUSED_STUDY_SESSION = "is_focused_study_session"
        private const val IS_START_IN_BACKGROUND = "is_start_in_background"
        private const val STUDY_TIME = "study_time"
        private const val MEDITATION_TYPE = "meditation_type"
        private const val OBJECT_GAZING_TYPE = "object_gazing_type"
        private var sInstance: PreferenceDataHelper? = null

        @Synchronized
        fun getInstance(context: Context): PreferenceDataHelper? {
            if (sInstance == null) {
                sInstance = PreferenceDataHelper(context)
            }
            return sInstance
        }
    }

    init {
        SharedPreferenceHelper.initialize(context)
        mSharedPreferenceHelper = SharedPreferenceHelper.getsInstance()!!
    }

    fun setAppList(dict: HashMap<String, Boolean>) {
        val dictString: String = Gson().toJson(dict)
        mSharedPreferenceHelper.setString(APP_LIST, dictString)
    }

    fun getAppList(): HashMap<String, Boolean>? {
        val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
        return Gson().fromJson(mSharedPreferenceHelper.getString(APP_LIST), type)
    }

    fun isStudyMode(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_STUDY_MODE, false)
    }

    fun setStudyMode(isStudyMode: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_STUDY_MODE, isStudyMode)
    }

    fun isFocusedStudySession(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_FOCUSED_STUDY_SESSION, false)
    }

    fun setFocusedStudySession(isFocusedStudySession: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_FOCUSED_STUDY_SESSION, isFocusedStudySession)
    }

    fun isStartInBackground(): Boolean {
        return mSharedPreferenceHelper.getBoolean(IS_START_IN_BACKGROUND, false)
    }

    fun setStartInBackground(isFocusedStudySession: Boolean) {
        return mSharedPreferenceHelper.setBoolean(IS_START_IN_BACKGROUND, isFocusedStudySession)
    }

    fun setStudyTime(studyTime: Int) {
        return mSharedPreferenceHelper.setInt(STUDY_TIME, studyTime)
    }

    fun getStudyTime(): Int {
        return mSharedPreferenceHelper.getInt(STUDY_TIME)
    }

    fun setMeditationType(meditationType: String) {
        return mSharedPreferenceHelper.setString(MEDITATION_TYPE, meditationType)
    }

    fun getMeditationType(): String? {
        return mSharedPreferenceHelper.getString(MEDITATION_TYPE)
    }

    fun setObjectGazingType(objectGazingType: String) {
        return mSharedPreferenceHelper.setString(OBJECT_GAZING_TYPE, objectGazingType)
    }

    fun getObjectGazingType(): String? {
        return mSharedPreferenceHelper.getString(OBJECT_GAZING_TYPE)
    }

    fun firstTimeAskingPermission(permission: String, isFirstTime: Boolean) {
        mSharedPreferenceHelper.setBoolean(permission, isFirstTime)
    }

    fun isFirstTimeAskingPermission(permission: String): Boolean {
        return mSharedPreferenceHelper.getBoolean(permission, true)
    }
}