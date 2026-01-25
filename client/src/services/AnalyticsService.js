import axios from 'axios';

const API = (process.env.REACT_APP_API || 'http://localhost:8080/api/').replace(/\/?$/, '/');
const auth = (token) => ({ headers: { Authorization: `Bearer ${token}` } });

export default class AnalyticsService {
    getPostAnalytics(postId, token, days = 7) {
        return axios.get(`${API}analytics/post/${postId}?days=${days}`, auth(token));
    }
}
