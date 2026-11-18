/**
 * QSmart - Comprehensive Multilingual Internationalization
 * Supported Languages:
 * - en: English
 * - hi: हिन्दी (Hindi)
 * - mr: मराठी (Marathi)
 * - gu: ગુજરાતી (Gujarati)
 * - bn: বাংলা (Bengali)
 * - te: తెలుగు (Telugu)
 * - ta: தமிழ் (Tamil)
 */

const languageNames = {
    en: "English",
    hi: "हिन्दी",
    mr: "मराठी",
    gu: "ગુજરાતી",
    bn: "বাংলা",
    te: "తెలుగు",
    ta: "தமிழ்"
};

const translations = {
    en: {
        app_title: "QSmart",
        app_subtitle: "Smart Virtual Queue Management System",
        nav_home: "Home",
        nav_patient: "Patient Portal",
        nav_staff: "Staff Desk",
        nav_doctor: "Doctor Clinic",
        nav_admin: "Admin Console",
        nav_public: "Waiting Room TV",
        nav_sms: "SMS Simulator",
        nav_login: "Login",
        nav_register: "Register",
        nav_logout: "Logout",
        nav_voice: "Voice Assistant",

        hero_tagline: "Inclusive & Intelligent Virtual Queue Management",
        hero_desc: "Join queues virtually from anywhere, receive digital token passes, track real-time waiting times, and experience zero physical waiting in hospital waiting rooms.",

        btn_book_token: "Book Token Now",
        btn_track_queue: "Track Live Queue",
        btn_call_next: "Call Next Patient",
        btn_mark_served: "Mark as Served",
        btn_skip_token: "Skip Patient",
        btn_report_delay: "Report Delay",
        btn_cancel_token: "Cancel Booking",

        channel_title: "One Centralized Queue • 3 Inclusive Channels",
        ch_smart_title: "1. Smartphone Users",
        ch_smart_desc: "Book from any browser, monitor live queue position, estimated wait times, and QR digital slip.",
        ch_sms_title: "2. Keypad SMS Users",
        ch_sms_desc: "No smartphone or internet required. Text simple commands like BOOK, STATUS, CANCEL via SMS.",
        ch_walkin_title: "3. Walk-in / Assisted Users",
        ch_walkin_desc: "Registered at reception counters. Enters the exact same centralized FIFO sequence for 100% fairness.",

        stat_waiting: "Waiting Ahead",
        stat_eta: "Estimated Wait",
        stat_current_serving: "Now Serving",
        stat_your_token: "Your Token",
        stat_dept: "Department",
        stat_hospital: "Hospital",

        table_token: "Token #",
        table_patient: "Patient",
        table_channel: "Channel",
        table_status: "Status",
        table_wait: "Est. Wait",
        table_actions: "Actions",

        fairness_notice: "Fairness Policy: Smartphone, SMS, and Walk-in patients enter the exact same queue sequence. Digital users do not unfairly jump ahead of walk-in patients."
    },
    hi: {
        app_title: "क्यूस्मार्ट (QSmart)",
        app_subtitle: "स्मार्ट वर्चुअल कतार प्रबंधन प्रणाली",
        nav_home: "होम",
        nav_patient: "मरीज पोर्टल",
        nav_staff: "स्टाफ डेस्क",
        nav_doctor: "डॉक्टर क्लिनिक",
        nav_admin: "एडमिन कंसोल",
        nav_public: "प्रतीक्षा कक्ष टीवी",
        nav_sms: "एसएमएस सिम्युलेटर",
        nav_login: "लॉगिन",
        nav_register: "पंजीकरण",
        nav_logout: "लॉगआउट",
        nav_voice: "आवाज सहायक",

        hero_tagline: "समावेशी एवं बुद्धिमान वर्चुअल कतार प्रबंधन",
        hero_desc: "कहीं से भी वर्चुअल कतार में शामिल हों, डिजिटल टोकन प्राप्त करें, लाइव प्रतीक्षा समय ट्रैक करें, और अस्पताल में बिना शारीरिक प्रतीक्षा के समय पर पहुंचें।",

        btn_book_token: "टोकन बुक करें",
        btn_track_queue: "लाइव कतार देखें",
        btn_call_next: "अगला मरीज बुलाएं",
        btn_mark_served: "सेवा पूर्ण मार्क करें",
        btn_skip_token: "टोकन छोड़ें (Skip)",
        btn_report_delay: "देरी की सूचना दें",
        btn_cancel_token: "बुकिंग रद्द करें",

        channel_title: "एक केंद्रीकृत कतार • 3 समावेशी माध्यम",
        ch_smart_title: "1. स्मार्टफोन उपयोगकर्ता",
        ch_smart_desc: "ब्राउज़र से टोकन बुक करें, वास्तविक समय में कतार स्थिति और अनुमानित समय ट्रैक करें।",
        ch_sms_title: "2. कीपैड फोन (SMS)",
        ch_sms_desc: "बिना इंटरनेट और स्मार्टफोन के साधारण फोन से BOOK, STATUS, CANCEL जैसे आसान SMS भेजें।",
        ch_walkin_title: "3. वॉक-इन (सहायता प्राप्त)",
        ch_walkin_desc: "अस्पताल रिसेप्शन पर स्टाफ द्वारा पंजीकृत। पूर्ण निष्पक्षता के लिए समान केंद्रीय कतार में प्रवेश।",

        stat_waiting: "आगे प्रतीक्षा में",
        stat_eta: "अनुमानित समय",
        stat_current_serving: "वर्तमान टोकन",
        stat_your_token: "आपका टोकन",
        stat_dept: "विभाग",
        stat_hospital: "अस्पताल",

        table_token: "टोकन क्र.",
        table_patient: "मरीज",
        table_channel: "माध्यम",
        table_status: "स्थिति",
        table_wait: "अनुमानित समय",
        table_actions: "कार्रवाई",

        fairness_notice: "निष्पक्षता नीति: स्मार्टफोन, एसएमएस और वॉक-इन मरीज एक ही कतार में शामिल होते हैं। डिजिटल उपयोगकर्ताओं को वॉक-इन मरीजों से अनुचित प्राथमिकता नहीं दी जाती।"
    },
    mr: {
        app_title: "क्यूस्मार्ट (QSmart)",
        app_subtitle: "स्मार्ट व्हर्च्युअल रांग व्यवस्थापन प्रणाली",
        nav_home: "मुख्यपृष्ठ",
        nav_patient: "रुग्ण पोर्टल",
        nav_staff: "कर्मचारी कक्ष",
        nav_doctor: "डॉक्टर क्लिनिक",
        nav_admin: "प्रशासक कन्सोल",
        nav_public: "प्रतीक्षा कक्ष टीव्ही",
        nav_sms: "एसएमएस सिम्युलेटर",
        nav_login: "लॉगिन",
        nav_register: "नोंदणी",
        nav_logout: "लॉगआउट",
        nav_voice: "आवाज सहाय्यक",

        hero_tagline: "सर्वसमावेशक आणि स्मार्ट व्हर्च्युअल रांग व्यवस्थापन",
        hero_desc: "कुठूनही व्हर्च्युअल रांगेत सामील व्हा, डिजिटल टोकन मिळवा, प्रतीक्षा वेळ तपासा आणि रुग्णालयातील प्रत्यक्ष रांगेचा त्रास टाळा.",

        btn_book_token: "आता टोकन बुक करा",
        btn_track_queue: "थेट रांग तपासा",
        btn_call_next: "पुढील रुग्णाला बोलवा",
        btn_mark_served: "सेवा पूर्ण झाली",
        btn_skip_token: "टोकन वगळा (Skip)",
        btn_report_delay: "विळंबाची माहिती द्या",
        btn_cancel_token: "बुकिंग रद्द करा",

        channel_title: "एकच केंद्रीय रांग • 3 सर्वसमावेशक मार्ग",
        ch_smart_title: "1. स्मार्टफोन वापरकर्ते",
        ch_smart_desc: "ब्राउझरवरून टोकन बुक करा, थेट रांग आणि अंदाजित वेळ पहा.",
        ch_sms_title: "2. कीपॅड फोन (SMS)",
        ch_sms_desc: "इंटरनेटशिवाय साध्या फोनवरून BOOK, STATUS, CANCEL संदेश पाठवा.",
        ch_walkin_title: "3. वॉक-इन (थेट रुग्ण)",
        ch_walkin_desc: "रिसेप्शनवर नोंदणी केली जाते. सर्वांना समान न्याय मिळण्यासाठी समान रांग.",

        stat_waiting: "रांगेत पुढे",
        stat_eta: "अंदाजित वेळ",
        stat_current_serving: "सुरू असलेले टोकन",
        stat_your_token: "आपले टोकन",
        stat_dept: "विभाग",
        stat_hospital: "रुग्णालय",

        table_token: "टोकन क्र.",
        table_patient: "रुग्ण",
        table_channel: "मार्ग",
        table_status: "स्थिती",
        table_wait: "अंदाजित वेळ",
        table_actions: "कृती",

        fairness_notice: "न्याय्य धोरण: स्मार्टफोन, एसएमएस आणि थेट येणारे रुग्ण एकाच रांगेत असतात. कोणावरही अन्याय होत नाही."
    },
    gu: {
        app_title: "ક્યુસ્માર્ટ (QSmart)",
        app_subtitle: "સ્માર્ટ વર્ચ્યુઅલ કતાર વ્યવસ્થાપન સિસ્ટમ",
        nav_home: "હોમ",
        nav_patient: "દર્દી પોર્ટલ",
        nav_staff: "સ્ટાફ ડેસ્ક",
        nav_doctor: "ડોક્ટર ક્લિનિક",
        nav_admin: "એડમિન કન્સોલ",
        nav_public: "વેઇટિંગ રૂમ ટીવી",
        nav_sms: "એસએમએસ સિમ્યુલેટર",
        nav_login: "લૉગિન",
        nav_register: "નોંધણી",
        nav_logout: "લૉગઆઉટ",
        nav_voice: "અવાજ સહાયક",

        hero_tagline: "સર્વસમાવેશી અને બુદ્ધિશાળી વર્ચ્યુઅલ કતાર વ્યવસ્થાપન",
        hero_desc: "કોઈપણ જગ્યાએથી વર્ચ્યુઅલ કતારમાં જોડાઓ, ડિજિટલ ટોકન મેળવો, લાઈવ સમય તપાસો અને કતારમાં ઊભા રહેવાથી બચો.",

        btn_book_token: "ટોકન બુક કરો",
        btn_track_queue: "લાઈવ કતાર જુઓ",
        btn_call_next: "આગળના દર્દીને બોલાવો",
        btn_mark_served: "સેવા પૂર્ણ થઈ",
        btn_skip_token: "ટોકન છોડો",
        btn_report_delay: "વિલંબની જાણ કરો",
        btn_cancel_token: "બુકિંગ રદ કરો",

        channel_title: "એક કેન્દ્રિય કતાર • 3 સર્વસમાવેશી માધ્યમ",
        ch_smart_title: "1. સ્માર્ટફોન વપરાશકર્તાઓ",
        ch_smart_desc: "બ્રાઉઝરથી ટોકન બુક કરો અને લાઈવ સમય જુઓ.",
        ch_sms_title: "2. કીપેડ ફોન (SMS)",
        ch_sms_desc: "ઇન્ટરનેટ વિના સાદા ફોન પરથી BOOK, STATUS, CANCEL મોકલો.",
        ch_walkin_title: "3. વોક-ઇન (સીધા આવનાર દર્દી)",
        ch_walkin_desc: "રિસેપ્શન પર નોંધણી. સમાન કેન્દ્રિય કતારમાં પ્રવેશ.",

        stat_waiting: "આગળ પ્રતીક્ષામાં",
        stat_eta: "અંદાજિત સમય",
        stat_current_serving: "હાલનો ટોકન",
        stat_your_token: "તમારો ટોકન",
        stat_dept: "વિભાગ",
        stat_hospital: "હોસ્પિટલ",

        table_token: "ટોકન નં.",
        table_patient: "દર્દી",
        table_channel: "માધ્યમ",
        table_status: "સ્થિતિ",
        table_wait: "અંદાજિત સમય",
        table_actions: "ક્રિયા",

        fairness_notice: "સમાનતા નીતિ: સ્માર્ટફોન, એસએમએસ અને વોક-ઇન દર્દીઓ એક જ કતારમાં પ્રવેશ કરે છે."
    },
    bn: {
        app_title: "কিউস্মার্ট (QSmart)",
        app_subtitle: "স্মার্ট ভার্চুয়াল সারি ব্যবস্থাপনা সিস্টেম",
        nav_home: "হোম",
        nav_patient: "রোগী পোর্টাল",
        nav_staff: "স্টাফ ডেস্ক",
        nav_doctor: "ডাক্তার ক্লিনিক",
        nav_admin: "অ্যাডমিন কনসোল",
        nav_public: "ওয়েটিং রুম টিভি",
        nav_sms: "এসএমএস সিমুলেটর",
        nav_login: "লগইন",
        nav_register: "নিবন্ধন",
        nav_logout: "লগআউট",
        nav_voice: "ভয়েস সহকারী",

        hero_tagline: "সবার জন্য বুদ্ধিমান ভার্চুয়াল সারি ব্যবস্থাপনা",
        hero_desc: "যেকোনো স্থান থেকে ভার্চুয়াল সারিতে যোগ দিন, ডিজিটাল টোকেন পান এবং দীর্ঘ অপেক্ষা এড়িয়ে চলুন।",

        btn_book_token: "টোকেন বুক করুন",
        btn_track_queue: "লাইভ সারি দেখুন",
        btn_call_next: "পরবর্তী রোগী ডাকুন",
        btn_mark_served: "সেবা সম্পন্ন",
        btn_skip_token: "টোকেন এড়িয়ে যান",
        btn_report_delay: "দেরির তথ্য দিন",
        btn_cancel_token: "বুকিং বাতিল করুন",

        channel_title: "একটি কেন্দ্রীয় সারি • ৩টি অন্তর্ভুক্তিমূলক মাধ্যম",
        ch_smart_title: "১. স্মার্টফোন ব্যবহারকারী",
        ch_smart_desc: "ব্রাউজার থেকে টোকেন বুক করুন এবং লাইভ সময় দেখুন।",
        ch_sms_title: "২. কিপ্যাড ফোন (SMS)",
        ch_sms_desc: "ইন্টারনেট ছাড়াই সাধারণ ফোন থেকে BOOK, STATUS, CANCEL পাঠান।",
        ch_walkin_title: "৩. সরাসরি আসা রোগী (Walk-in)",
        ch_walkin_desc: "হাসপাতালের কাউন্টারে নিবন্ধিত। সবার জন্য একই সমান সারি।",

        stat_waiting: "সামনে অপেক্ষমান",
        stat_eta: "আনুমানিক সময়",
        stat_current_serving: "বর্তমান টোকেন",
        stat_your_token: "আপনার টোকেন",
        stat_dept: "বিভাগ",
        stat_hospital: "হাসপাতাল",

        table_token: "টোকেন নং",
        table_patient: "রোগী",
        table_channel: "মাধ্যম",
        table_status: "অবস্থা",
        table_wait: "আনুমানিক সময়",
        table_actions: "পদক্ষেপ",

        fairness_notice: "ন্যায়নীতি: স্মার্টফোন, এসএমএস এবং সরাসরি আসা রোগীরা একই সারিতে যুক্ত হন।"
    },
    te: {
        app_title: "క్యూస్మార్ట్ (QSmart)",
        app_subtitle: "స్మార్ట్ వర్చువల్ క్యూ నిర్వహణ వ్యవస్థ",
        nav_home: "హోమ్",
        nav_patient: "రోగి పోర్టల్",
        nav_staff: "స్టాఫ్ డెస్క్",
        nav_doctor: "వైద్యుల క్లినిక్",
        nav_admin: "అడ్మిన్ కన్సోల్",
        nav_public: "వెయిటింగ్ రూమ్ టీవీ",
        nav_sms: "ఎస్ఎమ్ఎస్ సిమ్యులేటర్",
        nav_login: "లాగిన్",
        nav_register: "రిజిస్ట్రేషన్",
        nav_logout: "లాగ్అవుట్",
        nav_voice: "వాయిస్ అసిస్టెంట్",

        hero_tagline: "అందరికీ అందుబాటులో ఉండే స్మార్ట్ వర్చువల్ క్యూ సిస్టమ్",
        hero_desc: "ఎక్కడి నుంచైనా వర్చువల్ క్యూలో చేరండి, డిజిటల్ టోకెన్ పొందండి మరియు ఆసుపత్రిలో శారీరక నిరీక్షణను నివారించండి.",

        btn_book_token: "టోకెన్ బుక్ చేయండి",
        btn_track_queue: "లైవ్ క్యూ చూడండి",
        btn_call_next: "తదుపరి రోగిని పిలవండి",
        btn_mark_served: "సేవ పూర్తయింది",
        btn_skip_token: "టోకెన్ దాటవేయండి",
        btn_report_delay: "ఆలస్యం నివేదించండి",
        btn_cancel_token: "రద్దు చేయండి",

        channel_title: "ఒకే కేంద్రీకృత క్యూ • 3 సులభ మార్గాలు",
        ch_smart_title: "1. స్మార్ట్‌ఫోన్ వినియోగదారులు",
        ch_smart_desc: "బ్రౌజర్ నుండి టోకెన్ బుక్ చేసుకోండి మరియు లైవ్ క్యూ సమయం చూడండి.",
        ch_sms_title: "2. కీప్యాడ్ ఫోన్ (SMS)",
        ch_sms_desc: "ఇంటర్నెట్ లేకుండా సాధారణ ఫోన్ నుండి BOOK, STATUS పంపండి.",
        ch_walkin_title: "3. ప్రత్యక్ష రోగులు (Walk-in)",
        ch_walkin_desc: "రిసెప్షన్‌లో నమోదు చేసుకుంటారు. అందరికీ సమాన ప్రాధాన్యత.",

        stat_waiting: "ముందున్న వారు",
        stat_eta: "అంచనా సమయం",
        stat_current_serving: "ప్రస్తుత టోకెన్",
        stat_your_token: "మీ టోకెన్",
        stat_dept: "విభాగం",
        stat_hospital: "ఆసుపత్రి",

        table_token: "టోకెన్ సంఖ్య",
        table_patient: "రోగి",
        table_channel: "మార్గం",
        table_status: "స్థితి",
        table_wait: "అంచనా సమయం",
        table_actions: "చర్యలు",

        fairness_notice: "సమానత్వ విధానం: స్మార్ట్‌ఫోన్, ఎస్ఎమ్ఎస్ మరియు వాక్-ఇన్ రోగులు ఒకే క్యూలో చేరుతారు."
    },
    ta: {
        app_title: "க்யூஸ்மார்ட் (QSmart)",
        app_subtitle: "ஸ்மார்ட் மெய்நிகர் வரிசை மேலாண்மை அமைப்பு",
        nav_home: "முகப்பு",
        nav_patient: "நோயாளி போர்டல்",
        nav_staff: "ஊழியர் மேசை",
        nav_doctor: "மருத்துவர் கிளினிக்",
        nav_admin: "நிர்வாகக் கன்சோல்",
        nav_public: "காத்திருப்பு அறை டிவி",
        nav_sms: "எஸ்எம்எஸ் சிமுலேட்டர்",
        nav_login: "உள்நுழைவு",
        nav_register: "பதிவு",
        nav_logout: "வெளியேறு",
        nav_voice: "குரல் உதவியாளர்",

        hero_tagline: "அனைவருக்குமான அறிவார்ந்த மெய்நிகர் வரிசை மேலாண்மை",
        hero_desc: "எங்கிருந்தும் மெய்நிகர் வரிசையில் இணையுங்கள், டிஜிட்டல் டோக்கன் பெறுங்கள் மற்றும் காத்திருப்பு நேரத்தை தவிருங்கள்.",

        btn_book_token: "டோக்கன் முன்பதிவு செய்",
        btn_track_queue: "வரிசையை காண்க",
        btn_call_next: "அடுத்த நோயாளியை அழையுங்கள்",
        btn_mark_served: "சேவை முடிந்தது",
        btn_skip_token: "தவிர்க்கவும் (Skip)",
        btn_report_delay: "தாமதத்தை தெரிவி",
        btn_cancel_token: "ரத்து செய்",

        channel_title: "ஒரே மையப்படுத்தப்பட்ட வரிசை • 3 முறைகள்",
        ch_smart_title: "1. ஸ்மார்ட்போன் பயனர்கள்",
        ch_smart_desc: "வலைத்தளத்தில் டோக்கன் பதிவு செய்து நேரடி நிலையை பார்க்கவும்.",
        ch_sms_title: "2. சாதாரண கீபேட் போன் (SMS)",
        ch_sms_desc: "இணையம் இன்றி சாதாரண போனிலிருந்து BOOK, STATUS என அனுப்பலாம்.",
        ch_walkin_title: "3. நேரில் வருபவர்கள் (Walk-in)",
        ch_walkin_desc: "வரவேற்பறையில் பதிவு செய்யப்படும். அனைவருக்கும் சமமான ஒரே வரிசை.",

        stat_waiting: "முன்னால் காத்திருப்போர்",
        stat_eta: "எதிர்பார்க்கப்படும் நேரம்",
        stat_current_serving: "தற்போதைய டோக்கன்",
        stat_your_token: "உங்கள் டோக்கன்",
        stat_dept: "துறை",
        stat_hospital: "மருத்துவமனை",

        table_token: "டோக்கன் எண்",
        table_patient: "நோயாளி",
        table_channel: "முறை",
        table_status: "நிலை",
        table_wait: "நேரம்",
        table_actions: "செயல்கள்",

        fairness_notice: "சமத்துவ கொள்கை: ஸ்மார்ட்போன், எஸ்எம்எஸ் மற்றும் நேரில் வரும் நோயாளிகள் ஒரே வரிசையில் இணைக்கப்படுகிறார்கள்."
    }
};

let currentLang = localStorage.getItem('qsmart_lang') || 'hi';

function setLanguage(lang) {
    if (!translations[lang]) lang = 'en';
    currentLang = lang;
    localStorage.setItem('qsmart_lang', lang);
    applyLanguage();
    
    // Announce language change in Voice Assistant
    if (window.qsmartVoice) {
        window.qsmartVoice.onLanguageChanged(lang);
    }
}

function applyLanguage() {
    const dict = translations[currentLang] || translations.en;
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) {
            if (el.tagName === 'INPUT' && el.getAttribute('placeholder')) {
                el.placeholder = dict[key];
            } else {
                el.textContent = dict[key];
            }
        }
    });

    // Update active label in dropdown
    const labelEl = document.getElementById('currentLangLabel');
    if (labelEl) {
        labelEl.textContent = languageNames[currentLang] || 'Language';
    }

    // Update dropdown item highlights
    document.querySelectorAll('.lang-item').forEach(item => {
        if (item.getAttribute('data-lang') === currentLang) {
            item.classList.add('active', 'fw-bold');
        } else {
            item.classList.remove('active', 'fw-bold');
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    applyLanguage();
});
