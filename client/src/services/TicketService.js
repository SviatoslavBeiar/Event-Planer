// src/services/TicketService.js
import axios from 'axios';

export default class TicketService {
    register(postId, token) {
        return axios.post(
            process.env.REACT_APP_API + `tickets/register/${postId}`,
            {},
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }
    getMy(postId, token) {
        return axios.get(
            process.env.REACT_APP_API + `tickets/my/${postId}`,
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }

    // ВСІ мої квитки
    getMine(token) {
        return axios.get(
            process.env.REACT_APP_API + `tickets/mine`,
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }
}
