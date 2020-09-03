<template>
  <div id="app">
    <el-container class="main-container">
      <el-header class="main-header">
        <span class="header-font system-title">
          <i class="el-icon-s-promotion"></i>
          接口文档<el-button type="text" style="margin-left: 20px" @click="expandAll">{{isExpandAll?"折叠":"展开"}}所有</el-button>
        </span>
        <div class="main-header-person">
          <el-button type="success" icon="el-icon-user-solid" @click="openAuthDialog">Authentication</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside class="main-aside">
          <el-menu
            default-active="1"
            class="main-menu"
            background-color="#324157"
            text-color="#fff"
            active-text-color="#ffd04b"
            :default-openeds="moduleIndexList"
          >
            <el-submenu v-for="(item,i) in notEmptyList" :index="String(i)">
              <template slot="title">
                <i class="el-icon-s-tools"></i>
                <span>{{item.name}}</span>
              </template>

              <el-menu-item
                v-for="(api,j) in item.apiList"
                :index="String(i+''+j)"
                @click="openApiDetail(api)"
                v-if="api.name.trim().length>0"
              >
                <i class="el-icon-menu"></i>
                <span slot="title">{{api.name}}</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>
        <el-main>
          <api-detail :api="api"></api-detail>
        </el-main>
      </el-container>
    </el-container>
    <auth-dialog
      :dialog-visible="authVisible"
      @on-dialog-close="handleAuthClose"
    ></auth-dialog>
  </div>
</template>

<script>
  //测试时将json文件放到data文件夹
  //const moduleList = require("./data/data.json");
  import apiDetail from "./components/apiDetail";
  import authDialog from "./components/authDialog";
  import axios from 'axios'

  export default {
    name: "App",
    components: {
      apiDetail,authDialog
    },
    data() {
      return {
        moduleList: [],
        api: {},
        authVisible:false,
        moduleIndexList:[],
        isExpandAll:false
      };
    },
    methods: {
      openApiDetail(api) {
        this.api = api;
      },
      openAuthDialog(){
        this.authVisible=true;
      },
      handleAuthClose(){
        this.authVisible=false;
      },
      expandAll(){
        let moduleIndexList=[];
        if(!this.isExpandAll){
          let len=this.moduleList.length;
          for(let i=0;i<len;i++){
            moduleIndexList.push(i+'');
          }
        }
        this.isExpandAll=!this.isExpandAll;
        this.moduleIndexList=moduleIndexList;
      }
    },
    computed:{
      notEmptyList(){
        let notEmptyList=[];
        this.moduleList.forEach(item => {
          if(item.name.trim().length>0&&item.apiList.length>0){
            notEmptyList.push(item);
          }
        });
        return notEmptyList;
      }
    },
    mounted() {
      //发布时将json文件放到与index.html同级目录下
      let _t=this;
      axios.get('/doc/apiList.json').then(function(res){
        _t.moduleList = res.data;
      }).catch(function (error) {
        console.log(error);
      });
      //下面这句开发测试时解开，并将上面代码注释
      //this.moduleList=moduleList;
    }
  };
</script>

<style>
  #app {
    height: 100%;
    display: flex;
    flex-direction: column;
    margin: 0px;
  }

  .main-header {
    background-color: #242f42;
    text-align: center;
    line-height: 80px;
  }

  .el-footer {
    background-color: #b3c0d1;
    color: #333;
    text-align: center;
    line-height: 60px;
  }

  .el-aside {
    background-color: rgb(50, 65, 87);
    color: #333;
  }

  .el-main {
    background-color: white;
    color: #333;
    height: 100%;
    padding: 0px;
    display: flex;
    flex-direction: column;
  }

  .el-container:nth-child(5) .el-aside,
  .el-container:nth-child(6) .el-aside {
    line-height: 260px;
  }

  .el-container:nth-child(7) .el-aside {
    line-height: 320px;
  }

  html,
  body {
    height: 100%;
    margin: 0px;
  }

  .main-container {
    flex-grow: 1;
    height: 900px;
  }

  .main-header {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .main-header-person {
    display: flex;
    flex-direction: row-reverse;
    flex-grow: 1;
    align-items: flex-end;
  }

  .header-font {
    font-size: larger;
  }

  .main-header-person-info {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    margin-left: 10px;
    margin-right: 10px;
  }

  .main-header-person-setting {
    margin-left: 10px;
    font-size: 20px;
    line-height: 10px;
  }

  .main-header-person-info span {
    line-height: 20px;
    font-size: 16px;
    color: white;
  }

  .main-aside {
    display: flex;
    flex-direction: column;
    height: 900px;
  }

  .main-aside .main-menu {
    flex-grow: 1;
  }

  .system-title {
    line-height: 60px;
    color: white;
  }

  .el-main{
    height: 900px;
  }

  .el-menu{
    border: none;
  }
</style>
