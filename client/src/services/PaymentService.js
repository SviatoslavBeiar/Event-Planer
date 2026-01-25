import { api } from '../api/api';

export default class PaymentService {
    createCheckoutSession(postId) {
        return api.post(`payments/checkout-session/${postId}`, null);
    }
}
