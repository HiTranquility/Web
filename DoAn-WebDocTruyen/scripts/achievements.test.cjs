const {test} = require('node:test');
const assert = require('node:assert/strict');
const {fold, select, page} = require('../src/main/webapp/assets/js/achievements.js');
const items = [
 {index:0,name:'Cú Đêm',search:fold('Cú Đêm Đọc một chương ban đêm'),category:'Đọc truyện',earned:true,claimed:false,coins:5,progress:100},
 {index:1,name:'Nhà Phê Bình',search:fold('Nhà Phê Bình Viết đánh giá'),category:'Cộng đồng',earned:false,claimed:false,coins:10,progress:80},
 {index:2,name:'VIP',search:'vip',category:'Hội viên',earned:true,claimed:false,coins:0,progress:100},
 {index:3,name:'Về Đích',search:fold('Về Đích Hoàn thành truyện'),category:'Sáng tác',earned:true,claimed:true,coins:100,progress:100}
];
test('Vietnamese search folds accents and crossed d, matches all terms',()=>{
 assert.equal(fold('ĐÁNH GIÁ'), 'danh gia');
 assert.equal(select(items,{search:'cu dem chuong'}).length,1);
 assert.equal(select(items,{search:'danh viet'}).length,1);
 assert.equal(select(items,{search:'cu viet'}).length,0);
 assert.equal(select(items,{search:'<script>'}).length,0);
});
test('tabs and status filters distinguish earned, rewardless VIP and paid',()=>{
 assert.equal(select(items,{tab:'milestones'}).length,3);
 assert.deepEqual(select(items,{status:'claimable'}).map(a=>a.index),[0]);
 assert.deepEqual(select(items,{status:'claimed'}).map(a=>a.index),[3]);
 assert.deepEqual(select(items,{category:'Cộng đồng',status:'locked'}).map(a=>a.index),[1]);
});
test('near completion prioritizes unfinished and reward sort is deterministic',()=>{
 assert.equal(select(items,{sort:'progress'})[0].index,1);
 assert.equal(select(items,{sort:'coins'})[0].index,3);
 const before=items.map(a=>a.index);select(items,{sort:'name'});assert.deepEqual(items.map(a=>a.index),before);
});
test('pagination clamps after filtering including empty results',()=>{
 assert.equal(page(items,100,2).current,2);
 assert.deepEqual(page(items,2,2).items.map(a=>a.index),[2,3]);
 assert.deepEqual(page([],5,12),{current:1,pages:1,items:[]});
});
test('paid state immediately ceases to qualify for next filter action',()=>{
 const paid=items.map(a=>({...a}));paid[0].claimed=true;
 assert.equal(select(paid,{status:'claimable'}).length,0);
 assert.equal(select(paid,{status:'claimed'}).length,2);
});
function browserFixture(responder) {
 const vm=require('node:vm'),fs=require('node:fs');
 class Element {
  constructor(dataset={}) {this.dataset=dataset;this.events={};this.attrs={};this.textContent='';this.value='';this.hidden=false;this.children=[];}
  addEventListener(name,fn){this.events[name]=fn;}
  setAttribute(name,value){this.attrs[name]=value;}
  appendChild(el){this.children.push(el);}
  click(){this.events.click?.({target:this});}
  focus(options){this.focusOptions=options;}
 }
 const controls={};
 for(const name of ['search','category','status','sort','page-size','prev','next','reset','result-count','page','empty','claim-message','browser-controls','pagination']) controls['[data-'+name+']']=new Element();
 controls['#achievement-results']=new Element();
 const tabs=[new Element({tab:'perks'}),new Element({tab:'milestones'})];
 const views=[new Element({view:'grid'}),new Element({view:'list'})];
 const cards=Array.from({length:15},(_,i)=>{
  const card=new Element({achievement:'badge'+i,category:'Đọc truyện',earned:'true',claimed:'false',coins:'5',progress:'100'});
  card.querySelector=s=>({textContent:s==='h3'?'Huy hiệu '+i:'Đọc chương khác nhau'});
  return card;
 });
 const root=new Element();root.querySelector=s=>controls[s];
 root.querySelectorAll=s=>s==='[data-achievement]'?cards:s==='[role="tab"]'?tabs:s==='button[data-view]'?views:[];
 const wallet=new Element(),dropdown=new Element(),today=new Element(),total=new Element(),date=new Element(),claimedCoins=new Element(),claimedCount=new Element();let requests=0,prevented=0;
 const window={location:{hash:'#milestones'},scrollY:780,history:{replaceState(){}},localStorage:{getItem:()=> 'list',setItem(){}},
  fetch:async(...args)=>{requests++;return responder(...args);}};
 const document={createElement:()=>new Element(),querySelectorAll:s=>s==='[data-achievement-browser]'?[root]:s.includes('data-wallet-balance')?[wallet]:s==='[data-today-earned]'?[today]:s==='[data-total-earned]'?[total]:s==='[data-reward-date]'?[date]:s==='.dropdown-coin-tag'?[dropdown]:s.includes('data-claimed-coins')?[claimedCoins]:s.includes('data-claimed-count')?[claimedCount]:[]};
 vm.runInNewContext(fs.readFileSync(require.resolve('../src/main/webapp/assets/js/achievements.js'),'utf8'),{window,document,URLSearchParams,FormData:class {constructor(form){return new URLSearchParams(form.fields);}}});
 function submit(index) {
  const button=new Element();button.textContent='Nhận 5 xu';
  const form=new Element();form.fields={key:'badge'+index,_csrf:'synthetic-csrf',action:'claim-achievement'};
  form.action='/user';form.querySelector=()=>button;form.closest=()=>form;form.replaceWith=el=>{form.replaced=el;};
  const fire=()=>root.events.submit({target:form,preventDefault(){prevented++;}});fire();
  return {form,button,fire};
 }
 return {root,controls,tabs,cards,wallet,dropdown,today,total,date,claimedCoins,claimedCount,window,submit,requests:()=>requests,prevented:()=>prevented};
}
const tick=()=>new Promise(resolve=>setImmediate(resolve));
test('AJAX claim retains reward tab, list view, search, second page, scroll and paid card position',async()=>{
 const f=browserFixture(async()=>({ok:true,json:async()=>({claimed:true,balance:105,todayEarned:5,totalEarned:105,claimedCoins:5,claimedCount:1,rewardDate:'2026-10-19',message:'Đã nhận'})}));
 f.controls['[data-search]'].events.input({target:{value:'huy hieu'}});
 f.controls['[data-next]'].click();
 const before=f.controls['[data-page]'].textContent;
 const claim=f.submit(12);claim.fire();
 await tick();
 assert.equal(f.prevented(),2);assert.equal(f.requests(),1);
 assert.equal(f.root.dataset.tab,'milestones');assert.equal(f.root.dataset.view,'list');
 assert.equal(f.window.scrollY,780);assert.equal(f.controls['[data-page]'].textContent,before);
 assert.equal(f.cards[12].hidden,false);assert.equal(f.cards[12].dataset.claimed,'true');
 assert.equal(claim.form.replaced.textContent,'✓ Đã nhận');assert.equal(f.wallet.textContent,'105');
 assert.equal(f.dropdown.textContent,'105 xu');
 assert.equal(f.today.textContent,'5');assert.equal(f.total.textContent,'105');assert.equal(f.date.textContent,'2026-10-19');
 assert.equal(f.claimedCoins.textContent,'5');assert.equal(f.claimedCount.textContent,'1');
});
test('expired session, server rejection and lost response leave form retryable without crediting UI',async()=>{
 for(const responder of [async()=>({ok:false,json:async()=>({message:'Đăng nhập lại'})}),async()=>({ok:false,json:async()=>({message:'Chưa đạt'})}),async()=>{throw new Error('Mất kết nối');}]){
  const f=browserFixture(responder),claim=f.submit(0);await tick();
  assert.equal(claim.form.replaced,undefined);assert.equal(claim.button.disabled,false);
  assert.equal(claim.form.dataset.busy,'false');assert.equal(f.wallet.textContent,'');
  assert.ok(f.controls['[data-claim-message]'].textContent.length>0);
 }
});
test('different claims serialize to prevent stale wallet responses overriding a newer balance',async()=>{
 let finish;
 const f=browserFixture(()=>new Promise(resolve=>{finish=resolve;}));
 f.submit(0);f.submit(1);await tick();assert.equal(f.requests(),1);
 finish({ok:true,json:async()=>({claimed:true,balance:105,message:'OK'})});await tick();assert.equal(f.requests(),2);
 finish({ok:true,json:async()=>({claimed:true,balance:110,message:'OK'})});await tick();assert.equal(f.wallet.textContent,'110');
});
