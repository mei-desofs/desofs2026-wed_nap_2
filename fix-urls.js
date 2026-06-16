const fs = require('fs');
const c = JSON.parse(fs.readFileSync('ArcadeHaven.postman_collection.json', 'utf8'));

function fixUrl(urlRaw) {
  // For Postman URL format: {{baseUrl}}/api/... -> host: ["{{baseUrl}}"], path: ["api", ...]
  const parts = urlRaw.split('/');
  return {
    raw: urlRaw,
    host: [parts[0]],
    path: parts.slice(1)
  };
}

function fixUrlsInItems(items) {
  for (const item of items) {
    if (item.item) { fixUrlsInItems(item.item); continue; }
    if (item.request && item.request.url) {
      const u = item.request.url;
      if (u.raw && u.host && u.host[0] !== '{{baseUrl}}' && u.raw.startsWith('{{baseUrl}}')) {
        item.request.url = fixUrl(u.raw);
        console.log('Fixed URL for:', item.name);
      }
    }
  }
}

fixUrlsInItems(c.item);
fs.writeFileSync('ArcadeHaven.postman_collection.json', JSON.stringify(c, null, 2));
console.log('Done fixing URLs.');
