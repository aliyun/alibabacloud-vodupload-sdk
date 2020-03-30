//Copyright (C) 2020 Alibaba Group Holding Limited
const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');

module.exports = {
  mode: process.env.NODE_ENV,
  entry: {
    bundle: [path.resolve(__dirname, './src/main.js')],
  },
  output: {
    path: path.resolve(__dirname + '/dist'),
    publicPath: '/dist/',
    filename: 'js/[name].min.js'
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
    }
  },
  module: {
    rules: [{
      test: /\.js$/,
      use: 'babel-loader'
    }, {
      test: /\.vue$/,
      use: {
        loader: 'vue-loader',
        options: {
          loaders: {
            css: ExtractTextPlugin.extract({
              use: ['css-loader'],
              fallback: 'style-loader'
            })
          }
        }
      }
    }, {
      test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
      use: {
        loader: 'url-loader',
        options: {
          limit: 8192,
          name: 'images/[name].[hash:7].[ext]'
        }
      }
    }, {
      test: /\.(woff2?|eot|ttf|otf|)(\?.*)?$/, 
      use: {
        loader: 'url-loader',
        options: {
          limit: 8192,
          name: 'fonts/[name].[hash:7].[ext]'
        }
      }
    }, {
      test: /\.html$/,
      include: path.resolve(__dirname, 'src'),
      use: [{
        loader: 'html-loader',
        options: {
          interpolate: true
        }
      }]
    }, {
      test: /\.css$/,
      use: ExtractTextPlugin.extract({
        use: ['css-loader'],
        fallback: 'style-loader'
      })
    }]
  },
  devServer: {
    port: 3000,
    historyApiFallback: {
        index: '/dist/index.html',
    },
    contentBase: './',
    publicPath: '/dist/',
    hot: true,
  },
  plugins: [
    new ExtractTextPlugin({
      filename: 'css/style.css'   // 将 css 提取出来
    }),
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: './src/index.html',
      inject: 'body',
      hash: true
    }),
    new VueLoaderPlugin()
  ]
}

if (process.env.NODE_ENV === 'development') {
  module.exports.output.publicPath = '/dist/'
  module.exports.devtool = 'cheap-source-map'
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.HotModuleReplacementPlugin()
  ])
}

if (process.env.NODE_ENV === 'production') {
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('production')
    }),
    new UglifyJSPlugin({
      uglifyOptions: {
        compress: {
          drop_console: true,
          // pure_funcs: ['console.log']
        }
      }
    }),
    new OptimizeCssAssetsPlugin({
      cssProcessorOptions: { discardComments: { removeAll: true } },
      canPrint: true
    })
  ])
}
