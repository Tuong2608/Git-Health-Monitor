// Run from repository root: node docs/week-05/evidence/verify-wireframes.cjs
const {chromium}=require('../../../frontend/node_modules/@playwright/test');
const fs=require('node:fs'), path=require('node:path'), assert=require('node:assert/strict');
const {pathToFileURL}=require('node:url');
(async()=>{
 const browser=await chromium.launch({headless:true});
 const page=await browser.newPage({viewport:{width:1440,height:1000}});
 const errors=[],checks=[];page.on('pageerror',e=>errors.push(e.message));
 const requests=[];page.on('request',r=>{if(/^https?:/.test(r.url()))requests.push(r.url())});
 await page.goto(pathToFileURL(path.resolve(__dirname,'../wireframes/index.html')).href);
 for(const width of [1440,375]){
  await page.setViewportSize({width,height:1000});
  for(const screen of ['dashboard','hotspots','file','alerts']){
   await page.locator('nav [data-screen="'+screen+'"]').click();
   assert(await page.locator('#'+screen).isVisible());
   assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'horizontal page overflow');
   await page.screenshot({path:path.join(__dirname,`${screen}-${width}.png`),fullPage:true});
   checks.push(`${screen}: visible, no page overflow at ${width}px`);
  }
 }
 await page.setViewportSize({width:1440,height:1000});
 for(const state of ['loading','empty','error']){await page.locator('#state').selectOption(state);assert(await page.locator('#statebox').isVisible());assert(!(await page.locator('#content').isVisible()));checks.push('state '+state)}
 await page.screenshot({path:path.join(__dirname,'error-state.png')});
 await page.locator('#state').selectOption('ready');
 await page.locator('nav [data-screen="hotspots"]').click();
 await page.locator('#filter').fill('not-found');assert(await page.locator('#noresults').isVisible());
 await page.locator('#filter').fill('order');assert.equal(await page.locator('.hotrow:visible').count(),1);checks.push('filter and no results');
 await page.locator('nav [data-screen="file"]').click();await page.locator('#review').click();assert.match(await page.locator('#aiResult').innerText(),/Cần đồng ý/);
 await page.locator('#consent').check();
 for(const [mode,pattern] of [['timeout',/hết thời gian/],['large',/vượt giới hạn/],['ok',/AI-generated/]]){await page.locator('#aiMode').selectOption(mode);await page.locator('#review').click();assert.match(await page.locator('#aiResult').innerText(),pattern);checks.push('AI mock '+mode)}
 await page.locator('nav [data-screen="alerts"]').click();await page.locator('#interval').fill('30');assert.equal(await page.locator('#settings').evaluate(e=>e.checkValidity()),false);
 await page.locator('#interval').fill('1440');await page.locator('#settings button').click();assert.match(await page.locator('#saved').innerText(),/Đã lưu mô phỏng/);
 await page.locator('#ack').click();assert.equal(await page.locator('#alertStatus').innerText(),'ACKNOWLEDGED');await page.locator('#close').click();await page.locator('#ack').click();assert.equal(await page.locator('#alertStatus').innerText(),'CLOSED');checks.push('settings validation and alert state');
 await page.locator('nav [data-screen="dashboard"]').click();await page.locator('#compare').click();assert.match(await page.locator('#delta').innerText(),/0,07/);
 await page.locator('#analyse').click();await page.waitForFunction(()=>document.getElementById('job').textContent.includes('SUCCEEDED'));checks.push('comparison and mock job');
 assert.deepEqual(errors,[]);assert.deepEqual(requests,[]);checks.push('no JS errors; no HTTP requests');
 fs.writeFileSync(path.join(__dirname,'wireframe-results.json'),JSON.stringify({checkedAt:new Date().toISOString(),browser:await browser.version(),scope:'Offline mock only, not application E2E',checks,errors,httpRequests:requests},null,2)+'\n');
 console.log(`PASS: ${checks.length} checks; 9 screenshots`);await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
