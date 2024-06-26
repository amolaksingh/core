package com.wallet.core.managers

import androidx.navigation.NavController
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.slideFromBottom
import com.wallet.entities.Faq
import com.wallet.entities.FaqMap
import com.wallet.modules.markdown.MarkdownFragment
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import java.net.URL

object FaqManager {

    private val faqListUrl = App.appConfigProvider.faqUrl

    const val faqPathMigrationRequired = "management/migration_required.md"
    const val faqPathMigrationRecommended = "management/migration_recommended.md"
    const val faqPathPrivateKeys = "management/what-are-private-keys-mnemonic-phrase-wallet-seed.md"
    const val faqPathDefiRisks = "defi/defi-risks.md"

    private fun getFaqUrl(faqPath: String, language: String): String =
        URL(URL(faqListUrl), "faq/$language/$faqPath").toString()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Faq::class.java, FaqDeserializer(faqListUrl))
        .create()

    fun showFaqPage(navController: NavController, path: String, language: String = "en") {
        navController.slideFromBottom(
            R.id.markdownFragment,
            MarkdownFragment.Input(getFaqUrl(path, language), true, true)
        )
    }

    fun getFaqList(): Single<List<FaqMap>> {
        return Single.fromCallable {
            val request = Request.Builder()
                .url(faqListUrl)
                .build()

            val response = OkHttpClient().newCall(request).execute()

            val listType = object : TypeToken<List<FaqMap>>() {}.type
            val list: List<FaqMap> = gson.fromJson(response.body.charStream(), listType)
            response.close()

            list
        }
    }

    class FaqDeserializer(faqUrl: String) : JsonDeserializer<Faq> {
        private val faqUrlObj = URL(faqUrl)

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Faq {
            val jsonObject = json.asJsonObject

            return Faq(
                jsonObject["title"].asString,
                absolutify(jsonObject["markdown"].asString)
            )
        }

        private fun absolutify(relativeUrl: String?): String {
            return URL(faqUrlObj, relativeUrl).toString()
        }
    }
}
