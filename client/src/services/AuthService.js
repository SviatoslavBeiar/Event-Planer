import { api } from '../api/api';

export default class AuthService {
    register(values) {
        return api.post('auth/register', values);
    }

    login(values) {
        return api.post('auth/login', values);
    }

    adminGetPendingOrganizerRequests() {
        return api.get('admin/organizer-requests/pending');
    }

    adminApproveOrganizerRequest(id) {
        return api.post(`admin/organizer-requests/${id}/approve`);
    }

    adminRejectOrganizerRequest(id, body) {
        return api.post(`admin/organizer-requests/${id}/reject`, body || {});
    }

    verifyEmail(token) {
        return api.get('auth/verify-email', { params: { token } });
    }

    resendVerification(email) {
        return api.post('auth/resend-verification', null, { params: { email } });
    }
}
