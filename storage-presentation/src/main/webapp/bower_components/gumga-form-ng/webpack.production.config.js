const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

const baseName = "gumga-form";

module.exports = {
    entry: path.join(__dirname, 'src', 'index'),
    output: {
        path: path.join(__dirname, 'dist/'),
        filename: baseName + '.min.js'
    },
    plugins: [
        new UglifyJSPlugin(),
        new ExtractTextPlugin({
            filename: baseName + ".min.css",
            allChunks: true
        })
    ],
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: [
            {
              loader: 'babel-loader',
              options: {
                "presets": "es2015"
              }
            }
          ]
        },
        {
            test: /\.css$/,
            use: ExtractTextPlugin.extract({
                 use: 'css-loader'
            })
        },
        {
            test: /\.(jpe?g|png|gif|svg|eot|woff2|woff|ttf)$/i,
            use: "file-loader?name=assets/[name].[ext]"
        }]

    }
};
