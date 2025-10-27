// src/services/EventCheckerService.js
import axios from 'axios';

const API = process.env.REACT_APP_API;

export default class EventCheckerService {
    // Список чекерів для події
    list(postId, token) {
        return axios.get(`${API}event-checkers/by-post/${postId}`, {
            headers: { Authorization: 'Bearer ' + token },
        });
    }

    // Синонім (раптом ще десь використовується)
    byPost(postId, token) {
        return this.list(postId, token);
    }

    // Призначити
    assign(postId, userId, token) {
        return axios.post(`${API}event-checkers/assign/${postId}/${userId}`, {}, {
            headers: { Authorization: 'Bearer ' + token },
        });
    }

    // Зняти
    remove(postId, userId, token) {
        return axios.delete(`${API}event-checkers/revoke/${postId}/${userId}`, {
            headers: { Authorization: 'Bearer ' + token },
        });
    }

    // Синонім
    revoke(postId, userId, token) {
        return this.remove(postId, userId, token);
    }

    // (не обовʼязково) Перевірити, чи я — чекер
    amIChecker(postId, token) {
        return axios.get(`${API}event-checkers/am-i-checker/${postId}`, {
            headers: { Authorization: 'Bearer ' + token },
        });
    }

    // (не обовʼязково) Мої всі призначення
    mine(token) {
        return axios.get(`${API}event-checkers/mine`, {
            headers: { Authorization: 'Bearer ' + token },
        });
    }

    assignByEmail(postId, email, token) {
        return axios.post(
            `${API}event-checkers/assign-by-email/${postId}?email=${encodeURIComponent(email)}`,
            {},
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }

    removeByEmail(postId, email, token) {
        return axios.delete(
            `${API}event-checkers/revoke-by-email/${postId}?email=${encodeURIComponent(email)}`,
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }
}
