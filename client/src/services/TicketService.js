import { api } from '../api/api';

export default class TicketService {
    register(postId) {
        return api.post(`tickets/register/${postId}`, {});
    }

    getMy(postId) {
        return api.get(`tickets/my/${postId}`);
    }

    getMine() {
        return api.get('tickets/mine');
    }

    validate(postId, code) {
        return api.post('tickets/verify/validate', { postId, code });
    }

    consume(postId, code) {
        return api.post('tickets/verify/consume', { postId, code });
    }

    getAvailability(postId) {
        return api.get(`tickets/availability/${postId}`);
    }

    getMyPdf(postId) {
        return api.get(`tickets/my/${postId}/pdf`, { responseType: 'blob' });
    }

    // aliases щоб твій компонент не міняти
    verifyValidate(postId, code) {
        return this.validate(postId, code);
    }

    verifyConsume(postId, code) {
        return this.consume(postId, code);
    }
}
