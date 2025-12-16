import http from 'k6/http';
import { check } from 'k6';
import { randomItem, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const keywords = ['블랙', '화이트', '스커트', '셔츠', '팬츠', '딥레드 스커트'];
const categories = ['상의', '아우터', '바지', '원피스', '스커트'];
const genders = ['MEN', 'WOMEN', 'ALL'];

export const options = {
  vus: 1000,
  duration: '10s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const keyword = randomItem(keywords);
  const category = randomItem(categories);
  const gender = randomItem(genders);
  const page = randomIntBetween(0, 3);
  const params = [
    `keyword=${encodeURIComponent(keyword)}`,
    `categoryPaths=${encodeURIComponent(category)}`,
    `gender=${gender}`,
    `cursor=${page}`,
    'limit=30',
  ].join('&');
  const url = `${BASE_URL}/api/products?${params}`;

  const res = http.get(url, { tags: { endpoint: 'search' } });
  check(res, { 'search status 200': (r) => r.status === 200 });
}
