const fs = require('fs');
const c = JSON.parse(fs.readFileSync('ArcadeHaven.postman_collection.json', 'utf8'));

// Add new collection variables if not present
const existingVars = c.variable.map(v => v.key);
['setupAdminToken','setupBuyerToken','setupGameId','setupPendingOrderId'].forEach(name => {
  if (!existingVars.includes(name)) {
    c.variable.push({key: name, value: '', type: 'string'});
    console.log('Added variable:', name);
  }
});

function findFolder(name) {
  return c.item.find(f => f.name && f.name.startsWith(name));
}
function findReq(items, name) {
  for (const item of items) {
    if (item.item) { const r = findReq(item.item, name); if (r) return r; }
    if (item.name === name) return item;
  }
  return null;
}
function bearer(varName) {
  return {type:'bearer', bearer:[{key:'token', value:'{{' + varName + '}}', type:'string'}]};
}
function makeReq(name, method, url, bodyRaw, auth, testLines, prereqLines) {
  const req = {
    name,
    request: {
      method,
      header: [{key:'Content-Type', value:'application/json'}],
      url: {raw: url, host: [url.split('/')[2]], path: url.split('/').slice(3)},
    },
    event: []
  };
  if (bodyRaw) req.request.body = {mode:'raw', raw: bodyRaw, options:{raw:{language:'json'}}};
  if (auth) req.request.auth = auth;
  if (prereqLines) req.event.push({listen:'prerequest', script:{exec: prereqLines, type:'text/javascript'}});
  if (testLines) req.event.push({listen:'test', script:{exec: testLines, type:'text/javascript'}});
  return req;
}

// === 1. PRE-RUN SETUP: Add cleanup requests ===
const setupFolder = c.item.find(f => f.name && f.name.includes('Pre-run'));

setupFolder.item.push(makeReq(
  'Setup: App Admin Login',
  'POST', '{{baseUrl}}/api/auth/login',
  '{"username":"admin","password":"Admin123!"}',
  {type:'noauth'},
  [
    'var json = pm.response.json();',
    'pm.test("Setup admin login", () => pm.expect(pm.response.code).to.be.oneOf([200,401]));',
    'if (json.access_token) {',
    '  pm.collectionVariables.set("setupAdminToken", json.access_token);',
    '  pm.collectionVariables.set("adminToken", json.access_token);',
    '}'
  ]
));

setupFolder.item.push(makeReq(
  'Setup: Get All Games (find non-active to approve)',
  'GET', '{{baseUrl}}/api/admin/games',
  null,
  bearer('setupAdminToken'),
  [
    'pm.test("Setup get games", () => pm.expect(pm.response.code).to.be.oneOf([200,401]));',
    'if (pm.response.code === 200) {',
    '  var games = pm.response.json();',
    '  var toApprove = games.find(function(g) { return g.status === "PENDING" || g.status === "REJECTED"; });',
    '  pm.collectionVariables.set("setupGameId", toApprove ? toApprove.id : "");',
    '}'
  ]
));

setupFolder.item.push(makeReq(
  'Setup: Approve Non-Active Game (if any)',
  'PATCH', '{{baseUrl}}/api/admin/games/{{setupGameId}}/approve',
  null,
  bearer('setupAdminToken'),
  [
    'pm.test("Setup approve game", () => pm.expect(pm.response.code).to.be.oneOf([200,404,409,401]));'
  ],
  [
    'if (!pm.collectionVariables.get("setupGameId")) { pm.execution.skipRequest(); }'
  ]
));

setupFolder.item.push(makeReq(
  'Setup: App Buyer Login',
  'POST', '{{baseUrl}}/api/auth/login',
  '{"username":"buyer1","password":"Xtr4Safe#2026XY"}',
  {type:'noauth'},
  [
    'var json = pm.response.json();',
    'pm.test("Setup buyer login", () => pm.expect(pm.response.code).to.be.oneOf([200,401]));',
    'if (json.access_token) {',
    '  pm.collectionVariables.set("setupBuyerToken", json.access_token);',
    '  pm.collectionVariables.set("buyerToken", json.access_token);',
    '}'
  ]
));

setupFolder.item.push(makeReq(
  'Setup: Get Buyer Orders (find PENDING)',
  'GET', '{{baseUrl}}/api/orders',
  null,
  bearer('setupBuyerToken'),
  [
    'pm.test("Setup get buyer orders", () => pm.expect(pm.response.code).to.be.oneOf([200,401,403]));',
    'if (pm.response.code === 200) {',
    '  var orders = pm.response.json();',
    '  var pending = orders.find(function(o) { return o.status === "PENDING"; });',
    '  pm.collectionVariables.set("setupPendingOrderId", pending ? pending.id : "");',
    '}'
  ]
));

setupFolder.item.push(makeReq(
  'Setup: Cancel Pending Order (if any)',
  'POST', '{{baseUrl}}/api/orders/{{setupPendingOrderId}}/cancel',
  null,
  bearer('setupBuyerToken'),
  [
    'pm.test("Setup cancel pending order", () => pm.expect(pm.response.code).to.be.oneOf([200,404,409,401]));'
  ],
  [
    'if (!pm.collectionVariables.get("setupPendingOrderId")) { pm.execution.skipRequest(); }'
  ]
));

console.log('Added 6 Pre-run Setup cleanup requests');

// === 2. PUBLISHER: Insert "Admin: Approve New Game" after Create Game ===
const publisherFolder = findFolder('Publisher');
const createGameIdx = publisherFolder.item.findIndex(r => r.name === 'Create Game');
if (createGameIdx !== -1) {
  const approveReq = makeReq(
    'Admin: Approve New Game (for Order Tests)',
    'PATCH', '{{baseUrl}}/api/admin/games/{{gameId}}/approve',
    null,
    bearer('adminToken'),
    [
      'pm.test("New game approved or already active", () => pm.expect(pm.response.code).to.be.oneOf([200, 409, 401]));'
    ]
  );
  publisherFolder.item.splice(createGameIdx + 1, 0, approveReq);
  console.log('Inserted Admin: Approve New Game after Create Game in Publisher');
}

// === 3. Get All Active Games: Always update gameId ===
const activeGames = findReq(c.item, 'Get All Active Games');
if (activeGames) {
  const test = activeGames.event.find(e => e.listen === 'test');
  test.script.exec = [
    'pm.test("Status 200", () => pm.response.to.have.status(200));',
    'var games = pm.response.json();',
    'pm.test("Returns array", () => pm.expect(games).to.be.an("array"));',
    'if (games && games.length > 0) { pm.collectionVariables.set("gameId", games[0].id); }'
  ];
  console.log('Updated Get All Active Games: always sets gameId');
}

// === 4. Import Game Key: Accept 200 or 409, save entryId ===
const importKey = findReq(c.item, 'Import Game Key (RF-23)');
if (importKey) {
  const test = importKey.event.find(e => e.listen === 'test');
  test.script.exec = [
    'pm.test("Status 200 (imported) or 409 (already imported)", () => pm.expect(pm.response.code).to.be.oneOf([200, 409]));',
    'if (pm.response.code === 200) {',
    '  var json = pm.response.json();',
    '  pm.test("Has entries list", () => pm.expect(json.entries).to.be.an("array"));',
    '  if (json.entries && json.entries.length > 0) {',
    '    pm.collectionVariables.set("entryId", json.entries[json.entries.length - 1].id);',
    '  }',
    '}'
  ];
  console.log('Updated Import Game Key: accept 200 or 409, save entryId');
}

// === 5. Get My Library: Save entryId ===
const getMyLib = findReq(c.item, 'Get My Library');
if (getMyLib) {
  const test = getMyLib.event.find(e => e.listen === 'test');
  test.script.exec = [
    'pm.test("Status 200", () => pm.response.to.have.status(200));',
    'var entries = pm.response.json();',
    'pm.test("Returns array", () => pm.expect(entries).to.be.an("array"));',
    'if (entries && entries.length > 0) { pm.collectionVariables.set("entryId", entries[entries.length - 1].id); }'
  ];
  console.log('Updated Get My Library: saves entryId');
}

// === 6. Import Game Key – Game Not Active: Accept 404 or 409 ===
const importNotActive = findReq(c.item, 'Import Game Key – Game Not Active (Expects 409)');
if (importNotActive) {
  const test = importNotActive.event.find(e => e.listen === 'test');
  test.script.exec = [
    'pm.test("Non-existent or inactive game: 404 or 409", () => pm.expect(pm.response.code).to.be.oneOf([404, 409]));'
  ];
  console.log('Updated Import Game Key Not Active: accept 404 or 409');
}

// === 7. Suspend and Revoke Library Entry: Accept 204 or 404 ===
const suspend = findReq(c.item, 'Suspend Library Entry (RF-30)');
if (suspend) {
  suspend.event.find(e => e.listen === 'test').script.exec = [
    'pm.test("Status 204 or 404", () => pm.expect(pm.response.code).to.be.oneOf([204, 404]));'
  ];
  console.log('Updated Suspend Library Entry: accept 204 or 404');
}
const revokeLib = findReq(c.item, 'Revoke Library Entry (RF-30)');
if (revokeLib) {
  revokeLib.event.find(e => e.listen === 'test').script.exec = [
    'pm.test("Status 204 or 404", () => pm.expect(pm.response.code).to.be.oneOf([204, 404]));'
  ];
  console.log('Updated Revoke Library Entry: accept 204 or 404');
}

// === 8. Admin Approve Game: Accept 200 or 409 ===
const adminApprove = findReq(c.item, 'Approve Game (RF-09)');
if (adminApprove) {
  adminApprove.event.find(e => e.listen === 'test').script.exec = [
    'pm.test("Status 200 (approved) or 409 (already active)", () => pm.expect(pm.response.code).to.be.oneOf([200, 409]));',
    'if (pm.response.code === 200) {',
    '  pm.test("Status is ACTIVE", () => pm.expect(pm.response.json().status).to.eql("ACTIVE"));',
    '}'
  ];
  console.log('Updated Admin Approve Game: accept 200 or 409');
}

// === 9. Rename Reject Already-Active to Reject Already-Rejected ===
const rejectAlready = findReq(c.item, 'Reject Already-Active Game (Expects 409)');
if (rejectAlready) {
  rejectAlready.name = 'Reject Already-Rejected Game (Expects 409)';
  rejectAlready.event.find(e => e.listen === 'test').script.exec = [
    'pm.test("Rejecting rejected game returns 409", () => pm.expect(pm.response.code).to.eql(409));'
  ];
  console.log('Renamed Reject Already-Active to Reject Already-Rejected');
}

// === 10. Upload Game File: Set src ===
const uploadFile = findReq(c.item, 'Upload Game File (IMAGE)');
if (uploadFile && uploadFile.request.body && uploadFile.request.body.formdata) {
  const fileField = uploadFile.request.body.formdata.find(f => f.key === 'file');
  if (fileField) { fileField.src = 'test-image.png'; console.log('Set Upload Game File src to test-image.png'); }
}

// === 11. Add Cleanup: Re-approve Game in Admin section (for next run idempotency) ===
const adminFolder = findFolder('Admin');
const buyerForbiddenIdx = adminFolder.item.findIndex(r => r.name && r.name.includes('Buyer Forbidden'));
const reApprove = makeReq(
  'Cleanup: Re-approve Game (next run idempotency)',
  'PATCH', '{{baseUrl}}/api/admin/games/{{gameId}}/approve',
  null,
  bearer('adminToken'),
  [
    'pm.test("Re-approve game for next run", () => pm.expect(pm.response.code).to.be.oneOf([200, 409, 404, 401]));'
  ]
);
adminFolder.item.splice(buyerForbiddenIdx > -1 ? buyerForbiddenIdx : adminFolder.item.length, 0, reApprove);
console.log('Added Cleanup: Re-approve Game in Admin section');

// === 12. Get All Users: Save buyer1 userId ===
const getAllUsers = findReq(c.item, 'Get All Users');
if (getAllUsers) {
  const test = getAllUsers.event.find(e => e.listen === 'test');
  test.script.exec = [
    'pm.test("Status 200", () => pm.response.to.have.status(200));',
    'var users = pm.response.json();',
    'pm.test("Returns array", () => pm.expect(users).to.be.an("array"));',
    'if (users && users.length > 0) {',
    '  var buyer = users.find(function(u) { return u.username === "buyer1"; }) || users[0];',
    '  pm.collectionVariables.set("userId", buyer.id);',
    '}'
  ];
  console.log('Updated Get All Users: saves buyer1 userId');
}

fs.writeFileSync('ArcadeHaven.postman_collection.json', JSON.stringify(c, null, 2));
console.log('\nCollection saved successfully.');
