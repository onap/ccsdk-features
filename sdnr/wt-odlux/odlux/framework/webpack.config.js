/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
/**
 * Webpack 4 configuration file
 * see https://webpack.js.org/configuration/
 * see https://webpack.js.org/configuration/dev-server/
 */

"use strict";

const path = require("path");
const webpack = require("webpack");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const requirejsPlugin = require('requirejs-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

// const __dirname = (path => path.replace(/^([a-z]\:)/, c => c.toUpperCase()))(process.__dirname());

module.exports = (env) => {
  const distPath = path.resolve(__dirname, env === "release" ? "." : "..", "dist");
  const frameworkPath = path.resolve(__dirname, env === "release" ? "." : "..", "dist");
  return [{
    name: "Client",
    mode: "none", //disable default behavior
    target: "web",

    context: path.resolve(__dirname, "src"),

    entry: {
      app: [
        "./run.ts",
        "./app.tsx",
        "./services",
        "./components/objectDump",
        "./components/material-table",
        "./components/material-ui",
        "./utilities/elasticSearch",
        "./models"],
    },

    devtool: env === "release" ? false : "source-map",

    resolve: {
      extensions: [".ts", ".tsx", ".js", ".jsx"]
    },

    output: {
      path: distPath,
      library: "[name]", // related to webpack.DllPlugin::name
      libraryTarget: "umd2",
      filename: "[name].js",
      chunkFilename: "[name].js"
    },

    module: {
      rules: [{
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: [{
          loader: "babel-loader"
        }, {
          loader: "ts-loader"
        }]
      }, {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        use: [{
          loader: "babel-loader"
        }]
      }, 
      {
        //don't minify images
        test: /\.(png|gif|jpg|svg)$/,
        use: [{
          loader: 'url-loader',
          options: {
            limit: 10,
            name: './images/[name].[ext]'
          }
        }]
      }
      ]
    },

    optimization: {
      noEmitOnErrors: true,
      namedModules: env !== "release",
      minimize: env === "release",
      minimizer: env !== "release" ? [] : [new TerserPlugin({
        terserOptions: {
          mangle: {
            reserved: ["./app.tsx"]
          },
          warnings: false, // false, true, "verbose"
          compress: {
            drop_console: true,
            drop_debugger: true,
          }
        }
      })],
    },

    plugins: [
      new CopyWebpackPlugin([{
        from: '../../node_modules/requirejs/require.js',
        to: '.'
      }, {
        from: './favicon.ico',
        to: '.'
      }, {
        from: env === "release" ? './index.html' : 'index.dev.html',
        to: './index.html'
      }]),
      new requirejsPlugin({
        path: distPath,
        filename: 'config.js',
        baseUrl: '',
        pathUrl: '',
        processOutput: function (assets) {
          let mainConfig =  JSON.stringify(assets, null, 2);
          mainConfig = mainConfig.slice(0,-1); // remove closing bracket from string
          const entireConfig = mainConfig.concat(", waitSeconds: 30}"); // add waitSeconds to config
          return 'require.config(' + entireConfig + ')';
        }
      }),
      // new HtmlWebpackPlugin({
      //   filename: "index.html",
      //   template: "./index.html",
      //   inject: "head"
      // }),
      // new HtmlWebpackIncludeAssetsPlugin({
      //    assets: ['vendor.js'],
      //    append: false
      // }),
      new webpack.DllReferencePlugin({
        context: path.resolve(__dirname, "src"),
        manifest: require(path.resolve(frameworkPath, "vendor-manifest.json")),
        sourceType: "umd2"
      }),
      new webpack.DllPlugin({
        context: path.resolve(__dirname, "src"),
        name: "[name]",
        path: path.resolve(distPath, "[name]-manifest.json")
      }),
      ...(env === "release" ? [
        new webpack.DefinePlugin({
          "process.env": {
            NODE_ENV: "'production'",
            VERSION: JSON.stringify(require("./package.json").version)
          }
        }),
      ] : [
          new webpack.HotModuleReplacementPlugin(),
          new webpack.DefinePlugin({
            "process.env": {
              NODE_ENV: "'development'",
              VERSION: JSON.stringify(require("./package.json").version)
            }
          }),
          new webpack.WatchIgnorePlugin([
            /css\.d\.ts$/,
            /less\.d\.ts$/
          ]),
          new CopyWebpackPlugin([{
            from: './assets/version.json',
            to: './version.json'
          }])
        ])
    ],

    devServer: {
      public: "http://localhost:3100",
      contentBase: distPath,

      compress: true,
      headers: {
        "Access-Control-Allow-Origin": "*"
      },
      host: "0.0.0.0",
      port: 3100,
      disableHostCheck: true,
      historyApiFallback: true,
      inline: true,
      hot: false,
      quiet: false,
      stats: {
        colors: true
      },
      proxy: {
        "/about": {
          // target: "http://10.20.6.29:48181",
          target: "http://sdnr:8181",
          secure: false
        }, 
        "/yang-schema/": {
          target: "http://sdnr:8181",
          secure: false
        },   
        "/oauth/": {
          // target: "https://10.20.35.188:30205",
          target: "http://sdnr:8181",
          secure: false
        },
        "/oauth2/": {
          // target: "https://10.20.35.188:30205",
          target: "http://sdnr:8181",
          secure: false
        },
        "/database/": {
          target: "http://sdnr:8181",
          secure: false
        },
        "/restconf/": {
          target: "http://sdnr:8181",
          secure: false
        },
        "/rests/": {
          target: "http://sdnr:8181",
          secure: false
        },
        "/help/": {
          target: "http://sdnr:8181",
          secure: false
        },
         "/about/": {
          target: "http://sdnr:8181",
          secure: false
        },
        "/tree/": {
          target: "http://sdnr:8181",
          secure: false
        },
        "/websocket": {
          target: "http://sdnr:8181",
          ws: true,
          changeOrigin: true,
          secure: false
        },
        "/apidoc": {
          target: "http://sdnr:8181",
          ws: true,
          changeOrigin: true,
          secure: false
        }
      }
    }
  }];
}


