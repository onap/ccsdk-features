/**
 * Webpack 4 configuration file
 * see https://webpack.js.org/configuration/
 * see https://webpack.js.org/configuration/dev-server/
 */

"use strict";

const path = require("path");
const webpack = require("webpack");
const TerserPlugin = require('terser-webpack-plugin');

// const __dirname = (path => path.replace(/^([a-z]\:)/, c => c.toUpperCase()))(process.__dirname());

module.exports = (env) => {
  const distPath = path.resolve(__dirname, env === "release" ? "." : "..", "dist");
  const frameworkPath = path.resolve(__dirname, env === "release" ? "." : "..", "dist");
  return [{
    name: "App",

    mode: "none", //disable default behavior

    target: "web",

    context: path.resolve(__dirname, "src"),

    entry: {
      run: ["./run.ts"]
    },

    devtool: env === "release" ? false : "source-map",

    resolve: {
      extensions: [".ts", ".tsx", ".js", ".jsx"]
    },

    output: {
      path: distPath,
      filename: "[name].js",
      library: "[name]",
      libraryTarget: "umd2",
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
      }]
    },
    optimization: {
      noEmitOnErrors: true,
      namedModules: env !== "release",
      minimize: env === "release",
      minimizer: env !== "release" ? [] : [new TerserPlugin({
        terserOptions: {
          warnings: false, // false, true, "verbose"
          compress: {
            drop_console: true,
            drop_debugger: true,
          }
        }
      })],
    },
    plugins: [
      new webpack.DllReferencePlugin({
        context: path.resolve(__dirname, "./src"),
        manifest: require(path.resolve(frameworkPath, "app-manifest.json")),
        sourceType: "umd2"
      }),

    ]
  }
  ]
};