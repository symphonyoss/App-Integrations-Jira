import 'babel-polyfill';
import { initApp } from 'symphony-integration-commons';
import config from './config.service';
import IssueStateEnricher from '../enrichers/issueStateEnricher';

initApp(config, [new IssueStateEnricher()]);
