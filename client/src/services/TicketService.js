// src/services/TicketService.js
import axios from 'axios';


const API = (process.env.REACT_APP_API || 'http://localhost:8080/api/')
    .replace(/\/?$/, '/');

const auth = (token) => ({ headers: { Authorization: `Bearer ${token}` } });

export default class TicketService {
    register(postId, token) {
        return axios.post(`${API}tickets/register/${postId}`, {}, auth(token));
    }

    getMy(postId, token) {
        return axios.get(`${API}tickets/my/${postId}`, auth(token));
    }


    getMine(token) {
        return axios.get(`${API}tickets/mine`, auth(token));
    }


    validate(postId, code, token) {
        return axios.post(`${API}tickets/verify/validate`, { postId, code }, auth(token));
    }


    consume(postId, code, token) {
        return axios.post(`${API}tickets/verify/consume`, { postId, code }, auth(token));
    }


    verify(postId, code, token) {
        return axios.post(`${API}tickets/verify/${postId}`, { code }, auth(token));
    }


    getAvailability(postId, token) {
        return axios.get(`${API}tickets/availability/${postId}`, auth(token));
    }


    verifyValidate(postId, code, token) {
        return this.validate(postId, code, token);
    }

    verifyConsume(postId, code, token) {
        return this.consume(postId, code, token);
    }

    getMyPdf(postId, token) {
        return axios.get(
            process.env.REACT_APP_API + `tickets/my/${postId}/pdf`,
            { headers: { Authorization: 'Bearer ' + token }, responseType: 'blob' }
        );
    }

}
