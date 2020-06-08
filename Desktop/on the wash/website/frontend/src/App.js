import React, {Component} from 'react';

import 'bootstrap/dist/css/bootstrap.css';
//import 'bootstrap/dist/css/bootstrap-theme.css';
import NavBar from './NavBar/NavBar';
import ExampleAlert from './NavBar/ExampleAlert';
import Example from './ExampleNavbar.js'
import Home from './pages/home.js'


class App extends Component {
  render (){
    return(
      <div>
        <ExampleAlert/>
        <p>Welcome to On The Wash</p>
      </div>
    )
    }
}

export default App;
