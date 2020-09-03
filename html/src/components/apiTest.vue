<template>
  <div class="root" v-if="visible">
    <el-row>
      <h2>接口测试</h2>
      <el-button type="text" @click="back">返回</el-button>
    </el-row>
    <el-row>
      <el-col :span="16">
        <el-input placeholder="请求路径" v-model="api.path">
          <el-button type="primary" slot="append" @click="postRequest">Send</el-button>
        </el-input>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <h3>参数</h3>
        <el-button type="text" @click="toFormat()">格式化</el-button>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="16">
        <el-input
          type="textarea"
          :rows="6"
          placeholder="请输入参数"
          v-model="api.param">
        </el-input>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <h3>返回结果</h3>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <json-viewer copyable boxed :value="JSON.parse(api.result)"></json-viewer>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import axios from 'axios'
  export default {
        name: "apiTest",
        props: {
          api: {
            type: Object,
            default: {}
          },
          visible:{
            type: Boolean,
            default: false
          }
        },
        data(){
          return {
          };
        },
        methods:{
          postRequest(){
            let _t=this;
            let token = localStorage.getItem("token");
            let headers={
              'Content-Type': 'application/json'
            }
            if(token!==null){
              headers['Access-Token']=token;
            }
            axios.post(_t.api.path,JSON.parse(_t.api.param),{headers: headers}).then(res=>{
              _t.api.result=JSON.stringify(res.data);
            });
          },
          back(){
            this.$emit("on-close");
          },
          toFormat(){
            let obj = JSON.parse(this.api.param);
            this.api.param=JSON.stringify(obj, undefined, 4);
          }
        }
  }
</script>

<style scoped>
  .el-row {
    margin-top: 20px;
    margin-bottom: 20px;
  }
</style>
