package com.young.lineloginsample


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.jp22601.myapplication.R
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.android.synthetic.main.activity_line_login_intent.*
import org.json.JSONObject

class LineLoginIntentActivity : AppCompatActivity() {

    /**
     *
     * When onCreate, define WebView
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_line_login_intent)
        // get url from IntentActivity
        val url = this.intent.getStringExtra("url")
        // define webview, instead of findViewById(R.id.login_web_view)
        val lineLoginWebView: WebView = this?.login_web_view
        // define webview setting(optional)
        val webSettings = lineLoginWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        // define webclient
        lineLoginWebView.webViewClient = CusWebClient()
        // load line auth page link
        lineLoginWebView.loadUrl(url)
    }

    /**
     *
     * Customized web client for get AccessToken
     *
     */
    @SuppressLint("LongLogTag")
    class CusWebClient : WebViewClient() {
        // code data set; for test
        data class ResponseStateCode(val code: String, val state: String)

        // define static object
        companion object {
            // token url
            private val GET_TOKEN_URL: String = "https://api.line.me/oauth2/v2.1/token"
        }

        // LineLoginIntent -> webhook server -> Line auth server -> webhook server -> and view token
        // catch code from final url response
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

            // when received access token from Line server
            if (view.url.startsWith("https://line-message-young.herokuapp.com/getAuth?code=")) {
                val rs: ResponseStateCode = getStateCode(url) as ResponseStateCode

                var friends: JSONObject? = null
                var groups: JSONObject? = null
                try {
                    Fuel.post(GET_TOKEN_URL, listOf("grant_type" to "authorization_code", "code" to rs.code,
                            "redirect_uri" to "https://line-message-young.herokuapp.com/getAuth",
                            "client_id" to "", "client_secret" to "")).responseJson { req, res, result ->

                        val token: Any = JSONObject(String(res.data)).get("access_token")


                        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Bearer "+token)
                        Fuel.get("https://api.line.me/graph/v2/friends").responseJson { req_, res_, result_ ->


                            friends = JSONObject(String(res_.data))
                        }
                        Fuel.get("https://api.line.me/graph/v2/groups").responseJson { req_, res_, result_ ->

                            groups = JSONObject(String(res_.data))
                        }
                        view.loadData("FRIENDS = "+friends.toString()+"\nGROUPS = "+groups.toString(), null, null)
//                        view.loadUrl("https://api.line.me/friendship/v1/status", extraHeaders)

                    }

                } catch (e: Exception) {
                    Log.e("shit---------------------", e.message)
                } finally {
//                    view.destroy()
                }
            }
        }

        // get code from response
        fun getStateCode(reponseString: String): Any {
            val tempStringList: List<String> = reponseString.replace("https://line-message-young.herokuapp.com/getAuth?", "").split("&")
            fun getCode(x: String): String = when (x == "code") {
                is Any -> tempStringList[0].split("=")[1]
                else -> tempStringList[1].split("=")[1]
            }
            return ResponseStateCode(getCode(tempStringList[0].split("=")[1]), tempStringList[1].split("=")[1])
        }
    }
}