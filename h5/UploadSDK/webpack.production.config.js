//Copyright (C) 2020 Alibaba Group Holding Limited
const webpack = require('webpack');
const path = require('path');
const uglifyJsPlugin = webpack.optimize.UglifyJsPlugin;
const Merge = require('webpack-merge');

const baseWebpackConfig = require('./webpack.base.config.js');
let version = "1.5.1";

module.exports = Merge.smart(baseWebpackConfig, {
  output: {
    path: __dirname + '/disk',
    publicPath: '',
    filename: 'aliyun-upload-sdk-'+version+'.min.js'
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env':{
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new uglifyJsPlugin({
      sourceMap: true,
      compress: {
        warnings: false
      }
    })]
});
