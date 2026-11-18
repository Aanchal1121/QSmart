/**
 * QSmart - Real-time WebSocket Client with Polling Fallback
 */
class QSmartWebSocket {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.pollingIntervals = new Map();
    }

    connect(onConnectedCallback) {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('[QSmart WS] SockJS or Stomp not loaded, relying on polling fallback.');
            if (onConnectedCallback) onConnectedCallback(false);
            return;
        }

        try {
            const socket = new SockJS('/ws-queue');
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = null; // silence verbose frame logs

            this.stompClient.connect({}, (frame) => {
                this.connected = true;
                console.log('[QSmart WS] Connected to live queue WebSocket.');
                if (onConnectedCallback) onConnectedCallback(true);
            }, (error) => {
                this.connected = false;
                console.warn('[QSmart WS] WebSocket connection error. Activating HTTP polling fallback:', error);
                if (onConnectedCallback) onConnectedCallback(false);
                // Attempt reconnect in 10s
                setTimeout(() => this.connect(onConnectedCallback), 10000);
            });
        } catch (e) {
            console.error('[QSmart WS] Exception creating WebSocket:', e);
            if (onConnectedCallback) onConnectedCallback(false);
        }
    }

    subscribeDepartment(departmentId, onUpdate) {
        const topic = '/topic/queue/' + departmentId;
        if (this.connected && this.stompClient) {
            const sub = this.stompClient.subscribe(topic, (message) => {
                const payload = JSON.parse(message.body);
                onUpdate(payload);
            });
            this.subscriptions.set(topic, sub);
        }

        // Setup periodic fallback polling every 6 seconds as a guarantee
        if (!this.pollingIntervals.has('dept_' + departmentId)) {
            const interval = setInterval(() => {
                fetch('/api/public/queue/' + departmentId)
                    .then(res => res.json())
                    .then(json => {
                        if (json.success && json.data) {
                            onUpdate(json.data);
                        }
                    })
                    .catch(err => console.debug('[Poll fallback error]', err));
            }, 5000);
            this.pollingIntervals.set('dept_' + departmentId, interval);
        }
    }

    subscribePublicDisplay(onUpdate) {
        const topic = '/topic/public-display';
        if (this.connected && this.stompClient) {
            const sub = this.stompClient.subscribe(topic, (message) => {
                const payload = JSON.parse(message.body);
                onUpdate(payload);
            });
            this.subscriptions.set(topic, sub);
        }
    }

    subscribeUserNotifications(userId, onNotification) {
        if (!userId) return;
        const topic = '/topic/user/' + userId + '/notifications';
        if (this.connected && this.stompClient) {
            this.stompClient.subscribe(topic, (message) => {
                const payload = JSON.parse(message.body);
                onNotification(payload);
            });
        }
    }
}

window.qsmartWs = new QSmartWebSocket();
