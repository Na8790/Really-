package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Generates a smart travel itinerary based on user criteria.
     * Fallbacks to high-fidelity, hand-crafted local itineraries if key is missing or network fails.
     */
    suspend fun generateItinerary(
        city: String,
        durationDays: Int,
        budget: String,
        interests: String,
        isArabic: Boolean
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasRealKey = apiKey.isNotEmpty() && !apiKey.contains("MY_GEMINI_API_KEY")

        val prompt = if (isArabic) {
            "أنت مرشد سياحي يمني خبير في منصة 'تِجربة'. خطط لي برنامجاً سياحياً مخصصاً ومفصلاً في اليمن:\n" +
                    "- المدينة أو المنطقة: $city\n" +
                    "- عدد الأيام: $durationDays أيام\n" +
                    "- الميزانية المحددة: $budget\n" +
                    "- الاهتمامات: $interests\n\n" +
                    "الرجاء كتابة البرنامج باللغة العربية بتنسيق منظم يتضمن الأيام مع الأنشطة الصباحية والمسائية والمأكولات المحلية المقترحة لكل يوم ونقاط ثقافية بارزة. ركّز على التجارب المحلية الأصيلة والأماكن التاريخية الحقيقية."
        } else {
            "You are an expert Yemeni tour guide on the 'Tajrubah' platform. Plan a customized and detailed travel itinerary in Yemen:\n" +
                    "- City/Region: $city\n" +
                    "- Duration: $durationDays Days\n" +
                    "- Budget Level: $budget\n" +
                    "- Interests: $interests\n\n" +
                    "Please structure the response clearly with daily morning and evening activities, local food suggestions, and cultural tips. Emphasize authentic local experiences."
        }

        if (hasRealKey) {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrEmpty()) {
                    return responseText
                }
            } catch (e: Exception) {
                // Network or API failure -> Fall through to high-fidelity local generator
            }
        }

        // High-fidelity fallback generator if Gemini API key isn't provided or fails
        return getFallbackItinerary(city, durationDays, budget, interests, isArabic)
    }

    private fun getFallbackItinerary(
        city: String,
        durationDays: Int,
        budget: String,
        interests: String,
        isArabic: Boolean
    ): String {
        return if (isArabic) {
            buildString {
                append("✨ **خطة رحلتك الذكية من 'تِجربة' (الذكاء الاصطناعي - وضع محاكاة محلي)** ✨\n")
                append("📍 **الوجهة**: $city | **المدة**: $durationDays أيام | **الميزانية**: $budget | **الاهتمامات**: $interests\n\n")
                append("--- \n\n")

                for (day in 1..durationDays) {
                    append("📅 **اليوم $day**:\n")
                    when {
                        city.contains("صنعاء") || city.contains("Sana") -> {
                            if (day == 1) {
                                append("🌅 **الصباح**: زيارة باب اليمن التاريخي وتناول وجبة الفطور الصنعاني الأصيل (الفول والفتوت الحار).\n")
                                append("🕌 **بعد الظهر**: جولة ممتعة في أسواق صنعاء القديمة لشراء العطور والفضيات وحبات البن الحرازي.\n")
                                append("🍲 **الغداء**: تذوق وجبة السلتة أو الفحسة الساخنة في أحد مطاعم سوق الملح القديمة.\n")
                                append("🌆 **المساء**: الاسترخاء في سماسر صنعاء القديمة (سمسرة وردة) والاستماع لموشحات يمنية مع شرب قهوة القشر الفاخرة.\n")
                            } else {
                                append("🌅 **الصباح**: رحلة إلى دار الحجر الشهير في وادي ظهر، والتقاط صور تذكارية مذهلة للقصر المبني على صخرة شاهقة.\n")
                                append("🍲 **الغداء**: غداء محلي (مندي لحم بلدي في التنور) في حدائق وادي ظهر الخلابة.\n")
                                append("🌆 **المساء**: عودة إلى صنعاء وحضور جلسة شاي وحلويات الرواني في أحد المقاهي المطلة على منارات صنعاء الشامخة.\n")
                            }
                        }
                        city.contains("سقطرى") || city.contains("Socotra") -> {
                            if (day == 1) {
                                append("🌅 **الصباح**: الوصول إلى مطار حديبو والانتقال مباشرة إلى محمية ديتوا لاغون الخيالية ذات الرمال البيضاء الناعمة.\n")
                                append("🐠 **بعد الظهر**: جولة بحرية مع الكابتن عبدالله لمشاهدة الدلافين والتقاط القواقع والأسماك النادرة.\n")
                                append("🍲 **الغداء**: مأكولات بحرية طازجة معدة على طريقة الصيادين السقطريين.\n")
                                append("⛺ **المساء**: نصب الخيام وتناول شاي الزنجبيل الساخن تحت السماء الصافية المليئة بالنجوم.\n")
                            } else {
                                append("🌅 **الصباح**: الانطلاق باكراً نحو هضبة دكسم الشهيرة لمشاهدة غابات أشجار دم الأخوين الأسطورية والتمتع بالمناظر الجبلية الشامخة.\n")
                                append("🏊 **بعد الظهر**: السباحة في المسبح الطبيعي العذب في وادي ديرهور المحاط بالنخيل الشاهق.\n")
                                append("🌆 **المساء**: تناول وجبة عشاء يمنية دافئة والعودة إلى مخيم حديبو للراحة وسماع حكايات سكان الجزيرة.\n")
                            }
                        }
                        else -> {
                            append("🌅 **الصباح**: جولة سياحية استكشافية لأهم المعالم التراثية في $city والتعرف على كرم الضيافة اليمني.\n")
                            append("🍲 **الغداء**: وجبة شعبية تقليدية (زربيان أو حنيذ) من المطبخ اليمني الأصيل.\n")
                            append("🌆 **المساء**: زيارة المطلات الطبيعية وحضور غروب الشمس الساحر والاستمتاع بقهوة بن حراز العضوية الفاخرة.\n")
                        }
                    }
                    append("\n")
                }
                append("💡 **نصيحة ثقافية**: يعتبر كرم الضيافة جزءاً لا يتجزأ من الثقافة اليمنية؛ لا تتردد في قبول دعوات الشاي المحلية، فهي أفضل طريقة للتعرف على عادات وتقاليد البلد العريقة.")
            }
        } else {
            buildString {
                append("✨ **Your Smart Itinerary from Tajrubah (AI - Local Simulation Mode)** ✨\n")
                append("📍 **Destination**: $city | **Duration**: $durationDays Days | **Budget**: $budget | **Interests**: $interests\n\n")
                append("--- \n\n")

                for (day in 1..durationDays) {
                    append("📅 **Day $day**:\n")
                    when {
                        city.contains("صنعاء") || city.contains("Sana") -> {
                            if (day == 1) {
                                append("🌅 **Morning**: Visit the historic Bab Al-Yaman and enjoy a traditional Sana'ani breakfast (Foul and hot Fatoot bread).\n")
                                append("🕌 **Afternoon**: Walking tour in the bustling alleys of Old Sana'a's ancient souks, smelling local spices and inspecting hand-crafted silver.\n")
                                append("🍲 **Lunch**: Taste piping hot Saltah and Fahsa in a traditional clay pot inside the old salt market.\n")
                                append("🌆 **Evening**: Relax in the ancient Samsarah of Old Sana'a, enjoying premium spiced Qishr coffee with traditional chants.\n")
                            } else {
                                append("🌅 **Morning**: Excursion to the legendary Dar Al-Hajar (Rock Palace) in Wadi Dhar, capturing stunning photos of the palace perched on a single rock.\n")
                                append("🍲 **Lunch**: Savor local Mandi lamb in the lush gardens surrounding Wadi Dhar.\n")
                                append("🌆 **Evening**: Return to Sana'a for sweet Rawani cake and spiced tea at a rooftop café overlooking the majestic minarets.\n")
                            }
                        }
                        city.contains("سقطرى") || city.contains("Socotra") -> {
                            if (day == 1) {
                                append("🌅 **Morning**: Arrive in Hadiboh and transfer immediately to the magical Detwah Lagoon with its blindingly white sand dunes.\n")
                                append("🐠 **Afternoon**: Boat tour with local experts to watch friendly dolphins and explore pristine tidepools.\n")
                                append("🍲 **Lunch**: Enjoy fresh grilled fish cooked on the beach using local spices.\n")
                                append("⛺ **Evening**: Set up camp and drink hot herbal tea under one of the clearest star-studded skies in the world.\n")
                            } else {
                                append("🌅 **Morning**: Drive up to Dixam Plateau to see the ancient forests of endemic Dragon's Blood Trees.\n")
                                append("🏊 **Afternoon**: Hike down into the breathtaking canyon of Wadi Dirhur for a swim in crystal-clear freshwater pools.\n")
                                append("🌆 **Evening**: Savor local goat stew and return to Hadiboh camp, listening to beautiful local Socotri folk tales.\n")
                            }
                        }
                        else -> {
                            append("🌅 **Morning**: Exploratory tour of the historic sites of $city, interacting with warm and hospitable locals.\n")
                            append("🍲 **Lunch**: Relish a traditional Yemeni dish (Zurbian or Haneeth) made with aromatic spices.\n")
                            append("🌆 **Evening**: Watch the sunset from a local panoramic viewpoint, drinking authentic, organic Harazi coffee.\n")
                        }
                    }
                    append("\n")
                }
                append("💡 **Cultural Tip**: Hospitality is sacred in Yemen. Accepting an invitation to drink tea or coffee is the ultimate sign of respect and the best gateway to genuine local friendships.")
            }
        }
    }
}
