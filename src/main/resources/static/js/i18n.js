/**
 * QSmart - Bilingual Internationalization (English & Hindi)
 */
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
    }
};

let currentLang = localStorage.getItem('qsmart_lang') || 'en';

function setLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('qsmart_lang', lang);
    applyLanguage();
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

    // Update active state on language toggle buttons if present
    document.querySelectorAll('.lang-btn').forEach(btn => {
        if (btn.getAttribute('data-lang') === currentLang) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    applyLanguage();
});
