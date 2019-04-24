package com.young.lineloginsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.jp22601.myapplication.R
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.example.jp22601.login.TokenUtil.Companion.decrypt
import com.example.jp22601.login.TokenUtil.Companion.getCSRF
import com.example.jp22601.login.TokenUtil.Companion.hasher
import com.example.jp22601.login.TokenUtil.Companion.verifyToken
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection



class IntentActivity: AppCompatActivity() {

    var hashKey: String = ""
    var AUTH_CHANNEL_ID: String = ""
    var AUTH_WEBHOOK_REDIRECT: String = "https://line-message-young.herokuapp.com/getAuth"
    var lineLoginIntentActivity: Intent? = null
    var CONNECTON_TIMEOUT_MILLISECONDS = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_intent)

        lineLoginIntentActivity = Intent(this, LineLoginIntentActivity::class.java)

        val keyVal = findViewById(R.id.intent_txt_key_val) as TextView
        hashKey = hasher("test1")
        keyVal.setText(hashKey)

        val btn_intent_submit = findViewById(R.id.btn_intent_submit) as Button
        /*
        show the toast message
        send datas(email, kid's name, sha key), and wait callback again
            -server: nodejs, db: mongodb
            -registering to mongodb(email, kid's name, shakey but not line password
            -server will try to login to line because verify
        try to login by email to Line


         */

//        FuelManager.instance.basePath = "https://line-message-young.herokuapp.com";
//        FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
        btn_intent_submit.setOnClickListener {onClick(btn_intent_submit)}

        val intent = Intent(this, // current controller
                LocationTrackingService::class.java) // targeted component
        startService(intent) // start service
    }

    // on submit button click event
    fun onClick(btn: Button) {
        // line login
        val url = "https://access.line.me/oauth2/v2.1/authorize" +
        "?response_type=code" +
        "&client_id="+AUTH_CHANNEL_ID+
        "&redirect_uri="+AUTH_WEBHOOK_REDIRECT+
        "&state="+getCSRF(AUTH_CHANNEL_ID)+ // Use to cross-site request forgeries for MD5
        "&scope=profile%20openid%20email%20friends%20groups%20bot"

        lineLoginIntentActivity!!.putExtra("url", url)
        startActivityForResult(lineLoginIntentActivity, 1)

//        AuthenicationTask().execute(url)
//        lineLogin()

        // go to register TODO: only English is possible.
        //httpPostJson("IJIN JOUNG","hunyoung.joung@linecorp.com","test3")
        // return to main activity
//        finish()
    }

    // to go to Line login
    @SuppressLint("LongLogTag")
    fun lineLogin() {
        val parameter = listOf("response_type" to "code", "client_id" to AUTH_CHANNEL_ID, "redirect_uri" to AUTH_WEBHOOK_REDIRECT, "state" to getCSRF(AUTH_CHANNEL_ID), "scope" to "profile%20openid%20email")
        Fuel.get("https://access.line.me/oauth2/v2.1/authorize", parameter).responseJson { req, res, result ->
            //do something with response
            Log.d("result --------------------",result.toString())
            result.fold({ d ->
                Log.d("res header ok--------------------",res.httpResponseHeaders.toString())
                Log.d("res header ok--------------------",req.httpHeaders.toString())

                startActivityForResult(lineLoginIntentActivity, 1)
            }, { err ->
                Log.d("res header error --------------------",res.httpResponseHeaders.toString())
            })
        }
    }

    // on reflesh button click event

    // send to server for verification and restore
    @SuppressLint("LongLogTag")
    fun httpPostJson(kidName: String, email:String, password:String) {
        val token: String = TokenUtil.encrypt(kidName, hashKey) as String

        try {
            Fuel.post("https://line-message-young.herokuapp.com/registering", listOf("kidName" to kidName, "email" to email, "password" to password,"token" to token)).responseJson { req, res, result ->
                val token: String = res.httpResponseHeaders.get("token")!![0]
                Log.d("req header--------------------",req.httpHeaders.toString())
                Log.d("res header--------------------",res.httpResponseHeaders.toString())

                Log.d("from server--------------------",token)
                Log.d("by client--------------------",token)
                Log.d("by client decryption--------------------",decrypt(token.toByteArray(charset("utf-8")), hashKey))
                Log.d("verify--------------------", verifyToken(token, hashKey, kidName).toString())
            }

        } catch (e: Exception) {

            Log.e("shit---------------------",e.message)
        } finally {
            // message toasting
            Toast.makeText(this@IntentActivity, "Registering was done", Toast.LENGTH_LONG).show()

        }
    }


    @SuppressLint("LongLogTag")
    inner class AuthenicationTask : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            // Before doInBackground
            Log.d(">>>>> prepared execute >>>>> ","going")
        }

        fun streamToString(inputStream: InputStream): String {

            val bufferReader = BufferedReader(InputStreamReader(inputStream))
            var line: String
            var result = ""

            try {
                do {
                    line = bufferReader.readLine()
                    if (line != null) {
                        result += line
                    }
                } while (line != null)
                inputStream.close()
            } catch (ex: Exception) {

            }

            return result
        }

        override fun doInBackground(vararg urls: String?): String {

            var urlConnection: HttpsURLConnection? = null
            var inString: String? = ""
                    try {
                val url = URL(urls[0])

                urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.connectTimeout = CONNECTON_TIMEOUT_MILLISECONDS
                urlConnection.readTimeout = CONNECTON_TIMEOUT_MILLISECONDS

                        inString = streamToString(urlConnection.inputStream)

                publishProgress(inString)
            } catch (e: Exception) {
                Log.e(">>>>>> ERROR >>>>>>",e.message)
            } finally {
                Log.d(">>>>> after execute >>>>> ",inString)
                if (urlConnection != null) {
                    urlConnection.disconnect()
                }
                        lineLoginIntentActivity!!.putExtra("HTML_VALUE", inString)
                        startActivityForResult(lineLoginIntentActivity, 1)
            }

            return " "
        }

        override fun onProgressUpdate(vararg values: String?) {
            try {
                var json = JSONObject(values[0])

                Log.d(">>>>> RESULT >>>>> ",values.toString())
                startActivityForResult(lineLoginIntentActivity, 1)
//                val query = json.getJSONObject("query")
//                val results = query.getJSONObject("results")
//                val channel = results.getJSONObject("channel")
//
//                val location = channel.getJSONObject("location")
//                val city = location.get("city")
//                val country = location.get("country")
//
//                val humidity = channel.getJSONObject("atmosphere").get("humidity")
//
//                val condition = channel.getJSONObject("item").getJSONObject("condition")
//                val temp = condition.get("temp")
//                val text = condition.get("text")hun
//
//                tvWeatherInfo.text =
//                        "Location: " + city + " - " + country + "\n" +
//                        "Humidity: " + humidity + "\n" +
//                        "Temperature: " + temp + "\n" +
//                        "Status: " + text

            } catch (ex: Exception) {

            }
        }

        override fun onPostExecute(result: String?) {


            Log.d(">>>>> RESULT >>>>> ",result)
//            if (!result.isSuccess()) {
//                authenticationStatus.authenticationIntentHandled()
//                activity.onAuthenticationFinished(toErrorResult(response))
//                return
//            }
//            val oneTimePassword = response.getResponseData()
//            authenticationStatus.setOneTimePassword(oneTimePassword)
//            try {
//                val request = browserAuthenticationApi
//                        .getRequest(activity, config, oneTimePassword, permissions)
//                if (request.isLineAppAuthentication()) {
//                    // "launchMode" of the activity launched by the follows is "singleInstance".
//                    // So, we must not use startActivityForResult.
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        activity.startActivity(
//                                request.getIntent(),
//                                request.getStartActivityOptions())
//                    } else {
//                        activity.startActivity(request.getIntent())
//                    }
//                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        activity.startActivityForResult(
//                                request.getIntent(),
//                                REQUEST_CODE,
//                                request.getStartActivityOptions())
//                    } else {
//                        activity.startActivityForResult(
//                                request.getIntent(),
//                                REQUEST_CODE)
//                    }
//                }
//                authenticationStatus.setSentRedirectUri(request.getRedirectUri())
//            } catch (e: ActivityNotFoundException) {
//                authenticationStatus.authenticationIntentHandled()
//                activity.onAuthenticationFinished(LineLoginResult(
//                        LineApiResponseCode.INTERNAL_ERROR,
//                        LineApiError(e)))
//            }

        }
    }
}