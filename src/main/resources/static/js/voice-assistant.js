/**
 * QSmart - Intelligent Multilingual Voice Assistant (आवाज सहायक)
 * Features:
 * 1. Multilingual Speech Synthesis (TTS) in Hindi, English, Marathi, Gujarati, Bengali, Telugu, Tamil.
 * 2. Speech Recognition (STT) for interactive voice commands.
 * 3. Smart Contextual Q&A (Answers questions about active tokens, queue length, waiting times, and instructions).
 * 4. Automatic turn calling voice announcements.
 */

class QSmartVoiceAssistant {
    constructor() {
        this.synth = window.speechSynthesis;
        this.recognition = null;
        this.isListening = false;
        this.selectedVoice = null;
        this.initRecognition();
        this.injectVoiceUI();
    }

    initRecognition() {
        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (SpeechRec) {
            this.recognition = new SpeechRec();
            this.recognition.continuous = false;
            this.recognition.interimResults = false;
            this.recognition.maxAlternatives = 1;

            this.recognition.onstart = () => {
                this.isListening = true;
                this.updateMicUI(true);
                this.setAssistantStatus("Listening... बोलिए, मैं सुन रहा हूँ...");
            };

            this.recognition.onresult = (event) => {
                const speechResult = event.results[0][0].transcript;
                console.log("[QSmart Voice] Heard:", speechResult);
                this.displayUserMessage(speechResult);
                this.processVoiceCommand(speechResult);
            };

            this.recognition.onerror = (event) => {
                console.warn("[QSmart Voice] Speech recognition error:", event.error);
                this.isListening = false;
                this.updateMicUI(false);
                this.setAssistantStatus("माइक्रोफोन से आवाज नहीं आई। कृपया दोबारा बोलें या नीचे दिए गए बटन दबाएं।");
            };

            this.recognition.onend = () => {
                this.isListening = false;
                this.updateMicUI(false);
            };
        } else {
            console.warn("[QSmart Voice] Speech recognition not supported in this browser. Fallback to click-to-speak available.");
        }
    }

    getLangCode() {
        const lang = currentLang || 'hi';
        switch (lang) {
            case 'hi': return 'hi-IN';
            case 'mr': return 'mr-IN';
            case 'gu': return 'gu-IN';
            case 'bn': return 'bn-IN';
            case 'te': return 'te-IN';
            case 'ta': return 'ta-IN';
            case 'en':
            default: return 'en-IN';
        }
    }

    speak(text, onComplete) {
        if (!this.synth) return;
        this.synth.cancel(); // Stop any pending speech

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = this.getLangCode();
        utterance.rate = 0.95;
        utterance.pitch = 1.0;

        // Try to pick a native voice for the language if available
        const voices = this.synth.getVoices();
        const langPrefix = utterance.lang.split('-')[0];
        const matchingVoice = voices.find(v => v.lang.startsWith(langPrefix));
        if (matchingVoice) {
            utterance.voice = matchingVoice;
        }

        utterance.onend = () => {
            if (onComplete) onComplete();
        };

        this.synth.speak(utterance);
    }

    startListening() {
        if (!this.recognition) {
            this.speak("Speech recognition is not supported in this browser. Please use the quick response buttons below.");
            return;
        }
        try {
            this.recognition.lang = this.getLangCode();
            this.recognition.start();
        } catch (e) {
            console.debug("Recognition start error", e);
        }
    }

    stopListening() {
        if (this.recognition && this.isListening) {
            this.recognition.stop();
        }
    }

    toggleListening() {
        if (this.isListening) {
            this.stopListening();
        } else {
            this.startListening();
        }
    }

    async processVoiceCommand(rawQuery) {
        const query = rawQuery.toLowerCase();
        let reply = "";

        // Check if query is about token
        if (query.includes("token") || query.includes("टोकन") || query.includes("मेरा नंबर") || query.includes("my number")) {
            reply = await this.getMyTokenResponse();
        }
        // Check if query is about queue status or who is being served
        else if (query.includes("status") || query.includes("कतार") || query.includes("रांग") || query.includes("serving") || query.includes("कौन") || query.includes("चल रहा")) {
            reply = await this.getLiveQueueResponse();
        }
        // Check if query is about waiting time or people ahead
        else if (query.includes("wait") || query.includes("समय") || query.includes("कितना") || query.includes("देर") || query.includes("ahead") || query.includes("आगे")) {
            reply = await this.getWaitTimeResponse();
        }
        // Check if query is about booking
        else if (query.includes("book") || query.includes("बुक") || query.includes("कैसे")) {
            reply = this.getBookingHelpResponse();
        }
        // Check if query is language switch
        else if (query.includes("english") || query.includes("अंग्रेजी")) {
            setLanguage('en');
            reply = "Language switched to English. How can I help you with your queue token?";
        }
        else if (query.includes("hindi") || query.includes("हिंदी")) {
            setLanguage('hi');
            reply = "भाषा बदलकर हिंदी कर दी गई है। आप कतार या टोकन के बारे में कुछ भी पूछ सकते हैं।";
        }
        else {
            reply = currentLang === 'hi'
                ? `मैंने सुना: "${rawQuery}"। आप मुझसे अपना टोकन नंबर, कतार की स्थिति, या प्रतीक्षा समय पूछ सकते हैं।`
                : `I heard: "${rawQuery}". You can ask me for your token number, live queue status, or estimated waiting time.`;
        }

        this.displayAssistantMessage(reply);
        this.speak(reply);
    }

    async getMyTokenResponse() {
        try {
            const res = await fetch('/api/patient/tokens/active');
            const json = await res.json();
            if (json.success && json.data) {
                const t = json.data;
                if (currentLang === 'hi') {
                    return `आपका टोकन नंबर ${t.tokenNumber} है। यह ${t.departmentName} में है। आपके आगे ${t.peopleAhead} मरीज हैं और अनुमानित प्रतीक्षा समय लगभग ${t.estimatedWaitingMinutes} मिनट है।`;
                } else if (currentLang === 'mr') {
                    return `आपला टोकन क्रमांक ${t.tokenNumber} आहे. आपल्या पुढे ${t.peopleAhead} रुग्ण आहेत आणि अंदाजित वेळ ${t.estimatedWaitingMinutes} मिनिटे आहे.`;
                } else if (currentLang === 'gu') {
                    return `તમારો ટોકન નંબર ${t.tokenNumber} છે. તમારી આગળ ${t.peopleAhead} દર્દીઓ છે અને રાહ જોવાનો સમય ${t.estimatedWaitingMinutes} મિનિટ છે.`;
                } else {
                    return `Your active token number is ${t.tokenNumber} for ${t.departmentName}. There are ${t.peopleAhead} people ahead of you, and your estimated wait is approximately ${t.estimatedWaitingMinutes} minutes.`;
                }
            } else {
                return currentLang === 'hi'
                    ? "वर्तमान में आपका कोई सक्रिय टोकन नहीं है। आप 'टोकन बुक करें' बटन दबाकर नया टोकन ले सकते हैं।"
                    : "You do not have any active queue token currently. You can book one using the Book Token button.";
            }
        } catch (e) {
            return currentLang === 'hi' ? "टोकन जानकारी प्राप्त नहीं हो सकी।" : "Could not retrieve token information.";
        }
    }

    async getLiveQueueResponse() {
        try {
            const res = await fetch('/api/public/queue/1');
            const json = await res.json();
            if (json.success && json.data) {
                const q = json.data;
                const nowServing = q.currentServingToken !== 'None' ? q.currentServingToken : (currentLang === 'hi' ? 'कोई नहीं' : 'None');
                const counter = q.currentCounterNumber || 'Counter';
                if (currentLang === 'hi') {
                    return `जनरल ओपीडी में वर्तमान में टोकन नंबर ${nowServing} को ${counter} पर देखा जा रहा है। कतार में कुल ${q.totalWaiting} मरीज प्रतीक्षा कर रहे हैं।`;
                } else {
                    return `In General OPD, token number ${nowServing} is currently being served at ${counter}. There are ${q.totalWaiting} patients waiting in line.`;
                }
            }
        } catch (e) {
            console.debug(e);
        }
        return currentLang === 'hi' ? "कतार की स्थिति उपलब्ध नहीं है।" : "Queue status is currently unavailable.";
    }

    async getWaitTimeResponse() {
        return await this.getMyTokenResponse();
    }

    getBookingHelpResponse() {
        if (currentLang === 'hi') {
            return "टोकन बुक करने के 3 आसान तरीके हैं: पहला, वेबसाइट पर 'टोकन बुक करें' बटन दबाएं। दूसरा, साधारण फोन से एसएमएस भेजें 'BOOK HOSPITAL01 OPD' 56767 पर। तीसरा, अस्पताल रिसेप्शन काउंटर पर जाकर वॉक-इन टोकन लें।";
        } else {
            return "There are 3 ways to book a token: First, click the Book Token button on this dashboard. Second, text BOOK HOSPITAL01 OPD via SMS. Third, register as a walk-in at the hospital reception.";
        }
    }

    onLanguageChanged(lang) {
        const welcome = lang === 'hi' ? "नमस्ते, मैं आपका क्यूस्मार्ट आवाज सहायक हूँ। मैं आपकी क्या मदद कर सकता हूँ?" :
                        lang === 'mr' ? "नमस्कार, मी आपला क्यूस्मार्ट आवाज सहाय्यक आहे." :
                        lang === 'gu' ? "નમસ્તે, હું તમારો ક્યુસ્માર્ટ અવાજ સહાયક છું." :
                        "Hello, I am your QSmart Voice Assistant. How can I help you today?";
        this.displayAssistantMessage(welcome);
    }

    injectVoiceUI() {
        if (document.getElementById('qsmartVoiceContainer')) return;

        // 1. Floating Action Button
        const fab = document.createElement('div');
        fab.id = 'qsmartVoiceContainer';
        fab.innerHTML = `
            <!-- Floating Mic Pill Button -->
            <button id="voiceFabBtn" class="voice-fab shadow-lg" onclick="window.qsmartVoice.openModal()" title="Voice Assistant / आवाज सहायक">
                <i class="bi bi-mic-fill fs-4"></i>
                <span class="voice-fab-text d-none d-md-inline" data-i18n="nav_voice">आवाज सहायक</span>
            </button>

            <!-- Voice Assistant Modal -->
            <div class="modal fade" id="voiceAssistantModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content voice-modal-content border-0 shadow-lg">
                        <div class="modal-header bg-teal-dark text-white border-0 py-3">
                            <div class="d-flex align-items-center gap-2">
                                <div class="voice-header-icon">
                                    <i class="bi bi-robot fs-4"></i>
                                </div>
                                <div>
                                    <h6 class="modal-title fw-bold mb-0">QSmart Voice Assistant • आवाज सहायक</h6>
                                    <small class="text-white-50" id="assistantLangBadge">भाषा: हिन्दी (hi-IN)</small>
                                </div>
                            </div>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-4 bg-light">
                            <!-- Visual Chat Box -->
                            <div id="voiceChatHistory" class="voice-chat-box mb-3 p-3 bg-white rounded-3 border">
                                <div class="assistant-msg mb-2">
                                    <span class="badge bg-primary mb-1">आवाज सहायक</span>
                                    <div class="msg-text">नमस्ते! मैं आपका वर्चुअल कतार सहायक हूँ। आप बोलकर या नीचे दिए गए बटन दबाकर कतार की जानकारी ले सकते हैं।</div>
                                </div>
                            </div>

                            <!-- Animated Mic Wave & Status -->
                            <div class="text-center my-3">
                                <div id="voiceWaveAnimation" class="voice-wave d-none">
                                    <span></span><span></span><span></span><span></span><span></span>
                                </div>
                                <button id="voiceRecordBtn" class="btn voice-record-btn shadow" onclick="window.qsmartVoice.toggleListening()">
                                    <i class="bi bi-mic-fill fs-2"></i>
                                </button>
                                <div id="assistantStatusText" class="small text-muted mt-2 fw-semibold">
                                    माइक दबाकर बोलें (उदा. "मेरा टोकन क्या है?", "कतार में कितने लोग हैं?")
                                </div>
                            </div>

                            <!-- Quick Action Chips -->
                            <div class="mt-3 pt-3 border-top">
                                <small class="text-muted fw-bold d-block mb-2 text-uppercase">त्वरित प्रश्न (Quick Questions)</small>
                                <div class="d-flex flex-wrap gap-2">
                                    <button class="btn btn-sm btn-outline-primary voice-chip" onclick="window.qsmartVoice.processVoiceCommand('मेरा टोकन क्या है')">
                                        <i class="bi bi-ticket-perforated me-1"></i> मेरा टोकन बताओ
                                    </button>
                                    <button class="btn btn-sm btn-outline-success voice-chip" onclick="window.qsmartVoice.processVoiceCommand('कतार का स्टेटस क्या है')">
                                        <i class="bi bi-people me-1"></i> लाइव कतार स्थिति
                                    </button>
                                    <button class="btn btn-sm btn-outline-warning text-dark voice-chip" onclick="window.qsmartVoice.processVoiceCommand('टोकन कैसे बुक करें')">
                                        <i class="bi bi-info-circle me-1"></i> बुकिंग नियम
                                    </button>
                                    <button class="btn btn-sm btn-outline-secondary voice-chip" onclick="window.qsmartVoice.processVoiceCommand('Switch to English')">
                                        <i class="bi bi-translate me-1"></i> In English
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(fab);
    }

    openModal() {
        const modalEl = document.getElementById('voiceAssistantModal');
        if (modalEl) {
            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
            // Update lang badge
            const badge = document.getElementById('assistantLangBadge');
            if (badge) {
                badge.textContent = `Language: ${languageNames[currentLang] || 'English'} (${this.getLangCode()})`;
            }
        }
    }

    updateMicUI(listening) {
        const btn = document.getElementById('voiceRecordBtn');
        const wave = document.getElementById('voiceWaveAnimation');
        if (btn) {
            if (listening) {
                btn.classList.add('recording');
            } else {
                btn.classList.remove('recording');
            }
        }
        if (wave) {
            if (listening) wave.classList.remove('d-none');
            else wave.classList.add('d-none');
        }
    }

    setAssistantStatus(text) {
        const el = document.getElementById('assistantStatusText');
        if (el) el.textContent = text;
    }

    displayUserMessage(text) {
        const box = document.getElementById('voiceChatHistory');
        if (!box) return;
        const div = document.createElement('div');
        div.className = 'user-msg mb-2 text-end';
        div.innerHTML = `
            <span class="badge bg-secondary mb-1">आप (You)</span>
            <div class="msg-text bg-primary text-white p-2 rounded-3 d-inline-block text-start small">${text}</div>
        `;
        box.appendChild(div);
        box.scrollTop = box.scrollHeight;
    }

    displayAssistantMessage(text) {
        const box = document.getElementById('voiceChatHistory');
        if (!box) return;
        const div = document.createElement('div');
        div.className = 'assistant-msg mb-2';
        div.innerHTML = `
            <span class="badge bg-primary mb-1">आवाज सहायक</span>
            <div class="msg-text bg-light p-2 rounded-3 border small">${text}</div>
        `;
        box.appendChild(div);
        box.scrollTop = box.scrollHeight;
    }
}

// Global Voice Assistant instance
document.addEventListener('DOMContentLoaded', () => {
    window.qsmartVoice = new QSmartVoiceAssistant();
});
