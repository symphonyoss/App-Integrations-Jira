import React from 'react';
import ReactDOM from 'react-dom';
import './js/scripts.js';
export default class Setup extends React.Component {
	render() {
		return(
			<div>
				{/* Start editing area */}
				<h4>Step 1</h4>
				<p>Copy the URL so that you can use it later to configure the integration correctly.</p>
				<figure>
					<img src={require('./img/jira-admin-webhooks.jpg')} alt="Webhook configuration"/>
				</figure>
				<h4>Step 2</h4>
				<p>In your JIRA account, click on <strong>System</strong> in the <strong>Administration</strong> menu.</p>
				<figure>
					<img src={require('./img/jira_settings_step1.png')} alt="Webhook configuration"/>
				</figure>
				<h4>Step 3</h4>
				<p>Click on <strong>Webhooks</strong>, which is found in the sidebar under <strong>Advanced</strong>.</p>
				<figure>
					<img src={require('./img/jira_settings_step2.png')} alt="Webhook configuration"/>
				</figure>
				<h4>Step 4</h4>
				<p>Click the <strong>Create a Webhook</strong> button to display the webhook creation form. Choose a unique name and add the URL that you copied in step 1. You can also configure which events will be posted to Symphony. </p>
				<figure>
					<img src={require('./img/jira_settings_step3.png')} alt="Webhook configuration"/>
				</figure>
				<p>Click the Create button when you are done.</p>

				{/* End editing area */}
			</div>
		);
	}
}