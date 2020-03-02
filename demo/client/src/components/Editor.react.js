import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import superagent from 'superagent';
import WorkflowEditor from '@opuscapita/fsm4j-editor';
import componentsRegistry from '../customComponentsRegistry';
import { notificationSuccess, notificationError } from '../constants';

export default class Editor extends PureComponent {
  static contextTypes = {
    uiMessageNotifications: PropTypes.object.isRequired,
    url: PropTypes.func.isRequired
  }

  state = {}

  componentDidMount() {
    this.getWorkflowJSON()
  }

  getWorkflowJSON = _ => {
    const { uiMessageNotifications, url } = this.context;
    const self = this;
    superagent.
      get(url('/api/editordata')).
      then(res => {
        self.setState({ ...res.body })
      }).
      catch(err => {
        console.log(err);
        uiMessageNotifications.error({
          id: notificationError,
          message: 'Failed to load editor data: ' + err.message
        });
      })
  }

  handleSave = data => {
    console.log({ data });
    const { uiMessageNotifications, url } = this.context;
    superagent.
      post(url('/api/editordata')).
      send(data).
      then(res => {
        uiMessageNotifications.success({
          id: notificationSuccess,
          message: 'Workflow schema saved successfully'
        });
      }).
      catch(err => {
        console.log(err);
        uiMessageNotifications.error({
          id: notificationError,
          message: 'Save failed: ' + err.message
        });
      })
  }

  expressionEvaluator = ({code, arg}) => {
    return new Promise((response, reject) => {
      superagent.post(url("/api/eval")).send({code, arg})
        .then((res) => {
          const {result} = res.body;
          response(result)
        }).catch((err) => {
          const {result} = err.response.body;
        reject(new Error(result))
      });
    });
  };

  render() {
    const { schema, actions, conditions, objectConfiguration } = this.state;

    const props = {
      workflow: {
        schema,
        actions,
        conditions,
        objectConfiguration
      },
      expressionEvaluator: this.expressionEvaluator,
      componentsRegistry,
      onSave: this.handleSave
    }

    return schema ? (<WorkflowEditor {...props}/>) : null
  }
}
