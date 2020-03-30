//Copyright (C) 2020 Alibaba Group Holding Limited
const webpack = require('webpack');
const path = require('path');
const Merge = require('webpack-merge')

const baseWebpackConfig = require('./webpack.base.config.js')


module.exports = Merge.smart(baseWebpackConfig, {
  output: {
    filename: 'aliyun-upload-sdk.js'
  }
});
