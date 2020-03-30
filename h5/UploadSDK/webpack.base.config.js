//Copyright (C) 2020 Alibaba Group Holding Limited
const webpack = require('webpack');
const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: [
    path.resolve(__dirname, 'src/index.js')
  ],
  output: {
    path: __dirname + '/build/',
    publicPath: ''
  },
  module: {
    rules: [{
      test: /\.js[x]?$/,
      include: path.resolve(__dirname, 'src'),
      exclude: /node_modules/,
      use: [{
        loader:'babel-loader', 
        options: { presets: ["es2015","stage-0"] }
      }]
    }]
  },
  resolve: {
    extensions: ['.js'],
  },
  plugins: [
     new CopyWebpackPlugin([{
      from: __dirname + '/demo/*.*',
      to: __dirname + '/build/'
    },{
      from: __dirname + '/lib/*.*',
      to: __dirname + '/build/'
    }])
  ]
};
