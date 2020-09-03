<template>
  <el-dialog
    title="Authentication"
    :visible.sync="dialogVisible"
    :modal-append-to-body="false"
    width="40%"
    :before-close="handleClose"
    @open="handleOpen"
  >
    <el-form ref="formValidate" :model="formValidate" :rules="ruleValidate" label-width="150px">
      <el-form-item label="Access-Token" prop="token">
        <el-input v-model="formValidate.token" placeholder="输入token"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="handleConfirm('formValidate')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>

  export default {
    name: "authDialog",
    props: {
      dialogVisible: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        formValidate: {
          token:""
        },
        ruleValidate: {
        }
      };
    },
    methods: {
      handleClose() {
        this.$emit("on-dialog-close");
      },
      handleConfirm(name) {
        let _t=this;
        this.$refs[name].validate(valid => {
          if (valid) {
            let token=_t.formValidate.token;
            if(token===null||token.trim().length===0){
              localStorage.removeItem("token");
            }else{
              localStorage.setItem("token",token);
            }
            this.$notify.success("保存成功");
            this.$emit("on-dialog-close");
          }
        });
      },
      handleOpen(){
        let token=localStorage.getItem("token");
        if(token!==undefined){
          this.formValidate.token=token;
        }
      }
    }
  };
</script>

<style scoped>
</style>
