<template>
  <div class="root">
    <div v-if="!isTest">
    <el-row>
      <h2>{{api.name}}</h2>
      <el-button type="text" @click="openTest">接口测试</el-button>
    </el-row>
    <el-row>
      <el-col :span="2">
        <el-tag type="success">POST</el-tag>
      </el-col>
      <el-col :span="22">
        <el-tag type="info">{{api.path}}</el-tag>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <el-tag type="success">Content-type</el-tag>
      </el-col>
      <el-col :span="21">
        <el-tag type="info">application/json</el-tag>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <el-tag type="success">Request Headers</el-tag>
      </el-col>
      <el-col :span="21">
        <el-tag type="info">Access-Token: *******</el-tag>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="2">
        <el-tag type="success">传参格式</el-tag>
      </el-col>
      <el-col :span="16">
        <json-viewer copyable boxed :value="JSON.parse(api.param)"></json-viewer>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="2">
        <el-tag type="success">传参说明</el-tag>
      </el-col>
      <el-col>
        <el-table :data="api.paramList" style="width: 100%">
          <el-table-column property="fieldName" label="参数名" width="220"></el-table-column>
          <el-table-column label="必填" width="180">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isRequired" type="warning">是</el-tag>
              <el-tag v-if="!scope.row.isRequired" type="success">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column property="type" label="数据类型" width="180"></el-table-column>
          <el-table-column property="comment" label="说明"></el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="2">
        <el-tag type="success">返回结果</el-tag>
      </el-col>
      <el-col :span="16">
        <json-viewer copyable boxed :value="JSON.parse(api.result)"></json-viewer>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="2">
        <el-tag type="success">结果说明</el-tag>
      </el-col>
      <el-col>
        <el-table :data="api.resultList" style="width: 100%">
          <el-table-column property="fieldName" label="参数名" width="220">
            <template slot-scope="scope">
              <span v-html="scope.row.fieldName"></span>
            </template>
          </el-table-column>
          <el-table-column property="type" label="类型" width="180"></el-table-column>
          <el-table-column property="comment" label="说明"></el-table-column>
        </el-table>
      </el-col>
    </el-row>
    </div>
    <api-test :api="api" :visible="isTest" @on-close="closeTest"></api-test>
  </div>
</template>

<script>
  import apiTest from "./apiTest";
  export default {
    name: "apiDetail",
    props: {
      api: {
        type: Object,
        default: {}
      }
    },
    components:{
      apiTest
    },
    data() {
      return {
        isTest:false
      };
    },
    methods:{
      openTest(){
        this.isTest=true;
      },
      closeTest(){
        this.isTest=false;
      }
    }
  };
</script>

<style scoped>
  .el-tag {
    font-size: 16px;
  }

  .root {
    padding: 20px;
  }

  .el-row {
    margin-top: 20px;
    margin-bottom: 20px;
  }
</style>
