import React from 'react';
import {Link} from 'react-router-dom'
import {Bootstrap, Grid, Row, Col} from 'react-bootstrap';

function NavBar(){
    return(
        <nav className="navbar navbar-dark bg-primary fixed-top">
            <Link classname="navbar-brand" to ="/">
            TESTAP
            </Link>
        </nav>
    );
}
export default NavBar;