const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const baseName = "gumga-address";

module.exports = {
    entry: path.join(__dirname, 'src', 'address'),
    output: {
        path: path.join(__dirname, 'dist/'),
        filename: baseName+'.js',
        publicPath: '/dist/'
    },
    devServer: {
        inline: true,
        port: 1111
    },
    plugins: [
        new ExtractTextPlugin({
            filename: baseName+".css",
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
                        loader: 'babel-loader'
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
            }
        ]
    }
};