const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const plugins = [];

if (process.env.NODE_ENV === 'production') {
  plugins.push(new UglifyJsPlugin())
}

const config = {
  resolve: {
    alias: {
      '@opuscapita/fsm4j-editor': path.resolve(__dirname, '../../../editor')
    }
  },
  // context: path.resolve(__dirname, '../'),
  plugins,
  entry: path.resolve(__dirname, '../src/index.js'),
  output: {
    filename: 'editor-bundle.js',
    path: path.resolve(__dirname, '../../server/web-app/js'),
    library: 'fsm4jDemo',
    libraryTarget: 'umd',
  },
  externals: {
    react: 'React',
    'react-dom': 'ReactDOM'
  },
  watchOptions: {
    ignored: ['node_modules']
  },
  devtool: 'inline-source-map',
  module: {
    rules: [
      {
        test: /\.js$/,
        // include: path.resolve(__dirname, '../src'),
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
              ["env", {
                "targets": {
                  "browsers": ["last 2 versions", "ie >= 11", "safari >= 7", "Firefox ESR"]
                }
              }],
              'react'
            ],
            plugins: [
              "transform-decorators-legacy",
              "transform-class-properties",
              "transform-object-rest-spread",
            ]
          }
        },
        exclude: /node_modules/
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.less$/,
        use: ['style-loader', 'css-loader', 'less-loader']
      },
      {
        test: /\.(png|woff|woff2|eot|ttf)$/,
        loader: ['file-loader']
      },
      {
        test: /\.(svg)(\?[a-z0-9=&.]+)?$/,
        use: ['raw-loader']
      },
    ]
  }
}

module.exports = config;
