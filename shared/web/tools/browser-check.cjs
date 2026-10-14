// Optional verification: NODE_PATH must point to a playwright install, BROWSER_EXE to Chromium.
// Route transport delegates every request to real MockMvc over stdio; no API fixture is fabricated.
const {chromium}=require('playwright');
const fs=require('fs'); const path=require('path'); const readline=require('readline');
const assert=require('assert');
const input=readline.createInterface({input:process.stdin}); let waiting=[];
input.on('line',line=>{const next=waiting.shift();if(next)next(JSON.parse(line))});
function backend(message){return new Promise(resolve=>{waiting.push(resolve);process.stdout.write(JSON.stringify(message)+'\n')})}
const out=path.resolve(process.env.SCREENSHOT_DIR||path.join(__dirname,'../../../evidence/screenshots/web'));fs.mkdirSync(out,{recursive:true});
let serial=Promise.resolve();
(async()=>{
 const browser=await chromium.launch({executablePath:process.env.BROWSER_EXE,headless:true});
 const context=await browser.newContext({viewport:{width:1440,height:1100},timezoneId:'Asia/Shanghai'});
 await context.route('**/*',route=>{
   const run=async()=>{const req=route.request(),u=new URL(req.url());const reply=await backend({path:u.pathname+u.search,method:req.method(),headers:req.headers(),body:req.postData()||''});await route.fulfill({status:reply.status,contentType:reply.contentType,body:reply.body})};
   serial=serial.then(run);return serial;
 });
 const page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 const base='https://openatom.test';await page.goto(base);await page.locator('#events .empty').waitFor();
 await page.screenshot({path:path.join(out,'01-guest.png'),fullPage:true});
 const username='browser_'+Date.now();
 await page.locator('#loginButton').click();await page.locator('#authSwitch').click();
 await page.locator('#username').fill(username);await page.locator('#password').fill('BrowserPass2026');
 await page.locator('#authSubmit').click();await page.locator('#authDialog').waitFor({state:'hidden'});
 await page.screenshot({path:path.join(out,'02-login.png'),fullPage:true});
 await page.locator('#createButton').click();await page.locator('#title').fill('开放原子 · Java 并发实战');
 await page.locator('#description').fill('一起学习 Java、Spring Boot 和数据库锁，体验真实活动报名。');
 await page.locator('#location').fill('二教105');await page.locator('#capacity').fill('3');
 const future=new Date(Date.now()+7*86400000);const local=new Date(future.getTime()+8*3600000).toISOString().slice(0,16);
 await page.locator('#eventTime').fill(local);await page.locator('#editForm button[type=submit]').click();
 await page.locator('#editDialog').waitFor({state:'hidden'});await page.getByRole('heading',{name:'开放原子 · Java 并发实战',exact:true}).waitFor();
 await page.screenshot({path:path.join(out,'03-created.png'),fullPage:true});
 await page.getByRole('button',{name:'查看详情'}).first().click();
 await page.locator('#detailDialog').waitFor({state:'visible'});
 await page.getByRole('button',{name:'报名参加',exact:true}).click();
 await page.getByRole('button',{name:'已报名',exact:true}).waitFor();
 await page.screenshot({path:path.join(out,'04-registered.png'),fullPage:true});
 // Persistence: reload exercises token-only login, and registration state comes from the real backend.
 await page.reload();await page.getByRole('heading',{name:'开放原子 · Java 并发实战',exact:true}).waitFor();
 await page.waitForFunction(()=>document.getElementById('account').textContent.length>0);
 assert((await page.locator('#account').textContent()).includes(username));
 await page.getByRole('button',{name:'查看详情'}).first().click();await page.getByRole('button',{name:'编辑活动',exact:true}).click();
 await page.locator('#title').fill('开放原子 · Java 并发实战（更新）');await page.locator('#editForm button[type=submit]').click();
 await page.locator('#editDialog').waitFor({state:'hidden'});
 await page.getByRole('heading',{name:'开放原子 · Java 并发实战（更新）',exact:true}).waitFor();
 await page.getByRole('button',{name:'查看详情'}).first().click();
 page.once('dialog',d=>d.accept());await page.getByRole('button',{name:'取消活动',exact:true}).click();
 await page.waitForFunction(()=>document.querySelector('#detailContent .badge')?.textContent==='已取消');
 await page.locator('[data-close=detailDialog]').click();
 await page.screenshot({path:path.join(out,'05-cancelled.png'),fullPage:true});
 assert.deepStrictEqual(errors,[]);
 await page.setViewportSize({width:390,height:844});await page.screenshot({path:path.join(out,'06-mobile.png'),fullPage:true});
 const report={transport:'Playwright browser -> stdio -> real Spring MockMvc/security/JPA; no TCP due sandbox',
  checks:['guest list','register','login','create','detail','register event','token resume after reload','edit','cancel','mobile layout','no JS errors'],passed:true};
 fs.writeFileSync(path.join(out,'browser-result.json'),JSON.stringify(report,null,2));
 await browser.close();process.stdout.write('{"done":true}\n');input.close();
})().catch(e=>{fs.writeFileSync(path.join(out,'browser-failure.txt'),e.stack||String(e));console.error(e);process.stdout.write('{"done":true}\n');process.exit(1)});
