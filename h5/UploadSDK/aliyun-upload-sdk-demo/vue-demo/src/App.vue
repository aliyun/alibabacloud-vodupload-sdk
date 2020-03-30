<template>
  <div class="uploader">
    <div class="upload-type-switch">
      <input type="radio" id="one" value="one" v-model="picked">
      <label for="one" style="margin-right: 100px;">使用 UploadAuth 上传</label>
      <input type="radio" id="two" value="two" v-model="picked">
      <label for="two">使用 STSToken 上传</label>
    </div>
    <UploadAuth v-show="picked === 'one'" />
    <STSToken v-show="picked === 'two'" />
  </div>
</template>
<script>
  import axios from 'axios'
  import STSToken from './STSToken'
  import UploadAuth from './UploadAuth'

  export default {
    components: {
      STSToken,
      UploadAuth
    },
    data () {
      return {
        picked: 'one'
      }
    }
  }
</script>
<style>
  .upload-type-switch {
    text-align: center;
  }
  .container {
    width: 1200px;
    margin: 0 auto;
  }
  .input-control {
    margin: 5px 0;
  }
  .input-control label {
    font-size: 14px;
    color: #333;
    width: 30%;
    text-align: right;
    display: inline-block;
    vertical-align: middle;
    margin-right: 10px;
  }
  .input-control input {
    width: 30%;
    height: 30px;
    padding: 0 5px;
  }
  .upload {
    padding: 30px 50px;
  }
  .progress {
    font-size: 14px;
  }
  .progress i {
    font-style: normal;
  }
  .upload-type {
    color: #666;
    font-size: 12px;
    padding: 10px 0;
  }
  .upload-type button {
    margin: 0 10px 0 20px;
  }
  .status {
    font-size: 14px;
    margin-left: 30px;
  }
  .info {
    font-size: 14px;
    padding-left: 30px;
  }
</style>