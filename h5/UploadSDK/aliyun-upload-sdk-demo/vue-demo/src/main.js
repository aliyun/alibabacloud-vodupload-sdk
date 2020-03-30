//Copyright (C) 2020 Alibaba Group Holding Limited
import Vue from 'vue'
import App from './App.vue'
import "@babel/polyfill"
import axios from 'axios'
//兼容IE11
if (!FileReader.prototype.readAsBinaryString) {
    FileReader.prototype.readAsBinaryString = function (fileData) {
       var binary = "";
       var pt = this;
       var reader = new FileReader();      
       reader.onload = function (e) {
           var bytes = new Uint8Array(reader.result);
           var length = bytes.byteLength;
           for (var i = 0; i < length; i++) {
               binary += String.fromCharCode(bytes[i]);
           }
        //pt.result  - readonly so assign binary
        pt.content = binary;
        pt.onload()
    }
    reader.readAsArrayBuffer(fileData);
    }
}

new Vue({
  el: '#app',
  template: '<App/>',
  components: { App }
})
