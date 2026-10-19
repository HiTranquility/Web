const CACHE_NAME = 'doctruyen-pwa-v4';
const STATIC_ASSETS = [
    './assets/css/base.css?v=3.4',
    './assets/css/components.css?v=3.9',
    './assets/images/app-icon.svg',
    './manifest.json'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(STATIC_ASSETS).catch(() => {});
        })
    );
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.filter(key => key.startsWith('doctruyen-pwa-') && key !== CACHE_NAME)
                    .map(key => caches.delete(key))
            );
        })
    );
    self.clients.claim();
});

self.addEventListener('fetch', event => {
    if (event.request.method !== 'GET') return;
    const url = new URL(event.request.url);
    const scope = new URL(self.registration.scope);
    // Chỉ cache tài nguyên tĩnh. Trang, API, chương VIP và CSRF phụ thuộc phiên.
    if (url.origin !== scope.origin || !url.pathname.startsWith(scope.pathname + 'assets/')) return;

    // Network First với Cache Fallback cho CSS, JS, ảnh giao diện.
    event.respondWith(
        fetch(event.request)
            .then(networkResponse => {
                if (networkResponse && networkResponse.status === 200 && networkResponse.type === 'basic'
                        && !networkResponse.redirected
                        && !/no-store/i.test(networkResponse.headers.get('Cache-Control') || '')) {
                    const clone = networkResponse.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, clone);
                    });
                }
                return networkResponse;
            })
            .catch(() => {
                return caches.match(event.request).then(cached => cached || Response.error());
            })
    );
});
