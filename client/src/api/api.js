import axios from 'axios';

const baseURL = (process.env.REACT_APP_API || 'http://localhost:8080/api/')
    .replace(/\/?$/, '/');

export const api = axios.create({ baseURL });

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

api.interceptors.response.use(
    (res) => res,
    (error) => {
        const r = error?.response;
        const data = r?.data || {};
        const normalized = {
            status: r?.status ?? 0,
            message: data?.message || error?.message || 'Request failed',
            code: data?.code || null,
            fieldErrors: data?.fieldErrors || data?.validation || null,
            path: data?.path || null,
        };
        return Promise.reject(normalized);
    }
);
