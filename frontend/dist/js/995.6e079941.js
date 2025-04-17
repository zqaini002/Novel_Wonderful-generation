"use strict";(self["webpackChunknovel_assistant_frontend"]=self["webpackChunknovel_assistant_frontend"]||[]).push([[995],{3376:function(t,i,a){a.r(i),a.d(i,{default:function(){return D}});var s=a(6768),n=a(4232);const e={class:"admin-container"},l={class:"admin-stats"},c={class:"stat-card"},o={class:"stat-content"},d={class:"stat-card"},u={class:"stat-content"},r={class:"stat-card"},k={class:"stat-content"},v={class:"admin-actions"},L={class:"actions-grid"},b={class:"recent-activities"},p={key:0,class:"loading"},m={key:1,class:"no-data"},g={key:2,class:"activity-list"},h={class:"activity-time"},f={class:"activity-content"};function y(t,i,a,y,_,C){const w=(0,s.g2)("router-link");return(0,s.uX)(),(0,s.CE)("div",e,[i[14]||(i[14]=(0,s.Lk)("h1",null,"管理员控制台",-1)),(0,s.Lk)("div",l,[(0,s.Lk)("div",c,[i[1]||(i[1]=(0,s.Lk)("div",{class:"stat-icon"},[(0,s.Lk)("i",{class:"bi bi-people-fill"})],-1)),(0,s.Lk)("div",o,[(0,s.Lk)("h3",null,(0,n.v_)(_.stats.userCount||0),1),i[0]||(i[0]=(0,s.Lk)("p",null,"用户数量",-1))])]),(0,s.Lk)("div",d,[i[3]||(i[3]=(0,s.Lk)("div",{class:"stat-icon"},[(0,s.Lk)("i",{class:"bi bi-book-fill"})],-1)),(0,s.Lk)("div",u,[(0,s.Lk)("h3",null,(0,n.v_)(_.stats.novelCount||0),1),i[2]||(i[2]=(0,s.Lk)("p",null,"小说数量",-1))])]),(0,s.Lk)("div",r,[i[5]||(i[5]=(0,s.Lk)("div",{class:"stat-icon"},[(0,s.Lk)("i",{class:"bi bi-lightning-fill"})],-1)),(0,s.Lk)("div",k,[(0,s.Lk)("h3",null,(0,n.v_)(_.stats.activeUsers||0),1),i[4]||(i[4]=(0,s.Lk)("p",null,"活跃用户",-1))])])]),(0,s.Lk)("div",v,[i[10]||(i[10]=(0,s.Lk)("h2",null,"管理功能",-1)),(0,s.Lk)("div",L,[(0,s.bF)(w,{to:"/admin/dashboard",class:"action-card"},{default:(0,s.k6)((()=>i[6]||(i[6]=[(0,s.Lk)("i",{class:"bi bi-speedometer2"},null,-1),(0,s.Lk)("span",null,"仪表盘",-1)]))),_:1}),(0,s.bF)(w,{to:"/admin/users",class:"action-card"},{default:(0,s.k6)((()=>i[7]||(i[7]=[(0,s.Lk)("i",{class:"bi bi-people"},null,-1),(0,s.Lk)("span",null,"用户管理",-1)]))),_:1}),(0,s.bF)(w,{to:"/admin/novels",class:"action-card"},{default:(0,s.k6)((()=>i[8]||(i[8]=[(0,s.Lk)("i",{class:"bi bi-book"},null,-1),(0,s.Lk)("span",null,"小说管理",-1)]))),_:1}),(0,s.bF)(w,{to:"/admin/logs",class:"action-card"},{default:(0,s.k6)((()=>i[9]||(i[9]=[(0,s.Lk)("i",{class:"bi bi-list-ul"},null,-1),(0,s.Lk)("span",null,"系统日志",-1)]))),_:1})])]),(0,s.Lk)("div",b,[i[13]||(i[13]=(0,s.Lk)("h2",null,"最近活动",-1)),_.loading?((0,s.uX)(),(0,s.CE)("div",p,i[11]||(i[11]=[(0,s.Lk)("div",{class:"spinner-border text-primary",role:"status"},[(0,s.Lk)("span",{class:"visually-hidden"},"加载中...")],-1)]))):0===_.activities.length?((0,s.uX)(),(0,s.CE)("div",m,i[12]||(i[12]=[(0,s.Lk)("p",null,"暂无活动记录",-1)]))):((0,s.uX)(),(0,s.CE)("div",g,[((0,s.uX)(!0),(0,s.CE)(s.FK,null,(0,s.pI)(_.activities,((t,i)=>((0,s.uX)(),(0,s.CE)("div",{key:i,class:"activity-item"},[(0,s.Lk)("div",h,(0,n.v_)(C.formatDate(t.time)),1),(0,s.Lk)("div",f,[(0,s.Lk)("i",{class:(0,n.C4)(C.getActivityIcon(t.type))},null,2),(0,s.Lk)("span",null,(0,n.v_)(t.content),1)])])))),128))]))])])}var _={name:"AdminView",data(){return{loading:!1,stats:{userCount:25,novelCount:43,activeUsers:12},activities:[{type:"login",content:"用户 admin 登录了系统",time:new Date},{type:"upload",content:"用户 test1 上传了新小说《修真聊天群》",time:new Date(Date.now()-18e5)},{type:"register",content:"新用户 newuser123 注册了账号",time:new Date(Date.now()-72e5)},{type:"error",content:"系统发生错误：数据库连接中断",time:new Date(Date.now()-432e5)}]}},methods:{fetchAdminData(){this.loading=!0,setTimeout((()=>{this.loading=!1}),1e3)},formatDate(t){return new Date(t).toLocaleString("zh-CN",{month:"2-digit",day:"2-digit",hour:"2-digit",minute:"2-digit"})},getActivityIcon(t){const i={login:"bi bi-box-arrow-in-right",upload:"bi bi-cloud-upload",register:"bi bi-person-plus",error:"bi bi-exclamation-triangle",default:"bi bi-info-circle"};return i[t]||i.default}},mounted(){this.fetchAdminData()}},C=a(1241);const w=(0,C.A)(_,[["render",y],["__scopeId","data-v-42e17ad1"]]);var D=w}}]);