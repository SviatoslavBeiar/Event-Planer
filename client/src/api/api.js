import axios from 'axios';

export const api = axios.create({
    baseURL: process.env.REACT_APP_API, // напр. "http://localhost:8080/api/"
});

// додаємо токен автоматично
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// нормалізуємо помилки під ваш ApiError (status, message, code, fieldErrors, path)
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

        // опційно: авто-вихід на 401
        // if (normalized.status === 401) {
        //   localStorage.removeItem('token');
        //   window.location.href = '/login';
        // }

        return Promise.reject(normalized);
    }
);
