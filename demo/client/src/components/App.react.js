import React, { PureComponent } from 'react';
import { BrowserRouter as Router, Route, withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import { I18nManager } from '@opuscapita/i18n';
import { uiMessageNotifications } from '../uiGlobalComponents';
import Menu from './Menu.react';
import HomePage from './HomePage.react';
import Editor from './Editor.react';
import Logs from './Logs.react';
import WorkflowHistory from './History.react';
import { notificationSuccess, notificationError } from '../constants';
import './styles.css';

const url = baseUrl => path => `${baseUrl}${path}`;

export default class App extends PureComponent {
  static propTypes = {
    baseUrl: PropTypes.string
  }

  static defaultProps = {
    baseUrl: '/fsmDemo'
  }

  static childContextTypes = {
    i18n: PropTypes.object.isRequired,
    uiMessageNotifications: PropTypes.object.isRequired,
    url: PropTypes.func.isRequired
  }

  constructor(...args) {
    super(...args);
    this.i18n = new I18nManager();
  }

  getChildContext() {
    return {
      i18n: this.i18n,
      uiMessageNotifications,
      url: url(this.props.baseUrl)
    }
  }

  componentWillUnmount() {
    uiMessageNotifications.remove({ id: notificationSuccess })
    uiMessageNotifications.remove({ id: notificationError })
  }

  render() {
    const MyMenu = withRouter(Menu);

    return (
      <Router basename={this.props.baseUrl + '/fsmDemo'}>
        <div>
          <MyMenu/>
          <Route exact={true} path='/' component={HomePage}/>
          <Route exact={true} path='/editor' component={Editor}/>
          <Route exact={true} path='/logs' component={Logs}/>
          <Route path='/invoice/:objectId' component={WorkflowHistory}/>
        </div>
      </Router>
    )
  }
}
