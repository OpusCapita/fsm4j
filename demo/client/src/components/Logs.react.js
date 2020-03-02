import React from "react"
import superagent from 'superagent';

export default class Logs extends React.Component {
  static contextTypes = {
    url: PropTypes.func.isRequired
  }

  state = {
    offset: 0,
    events: []
  };

  loadEvents = () => {
    const { url } = this.context;
    const {offset, events} = this.state;
    superagent.get(url(`/api/logEvents?offset=${offset}`)).then(res => {
      if (offset !== res.body.offset) {
        this.setState({offset: res.body.offset, events: res.body.events.concat(events).sort().reverse()});
      }
    }).catch(console.error);
  };

  componentDidMount() {
    this.loadEvents();
    this.timerId = setInterval(() => {
      this.loadEvents();
    }, 1000);
  }

  componentWillUnmount() {
    clearInterval(this.timerId);
  }

  render() {
    const {events} = this.state;
    return (
      <div style={{overflowY: "scroll", height: "600px", marginLeft: "20px", marginRight: "20px"}}>
        {events.map((event, index) => {
          return (
            <div key={index}>
              {event}
            </div>
          );
        })}
      </div>
    );
  }
}