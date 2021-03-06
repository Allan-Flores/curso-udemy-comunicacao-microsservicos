import amqp from "amqplib/callback_api.js";
import { listenToSalesConfirmationQueue } from "../../modules/sales/rabbitmq/salesConfirmationListener.js";

import {
    PRODUCT_TOPIC,
    PRODUCT_STOCK_UPDATE_QUEUE,
    PRODUCT_STOCK_UPDATE_ROUTING_KEY,
    SALES_CONFIRMATION_QUEUE,
    SALES_CONFIRMATION_ROUTING_KEY
} from "./queue.js";

import { RABBIT_MQ_URL } from "../constans/secrets.js";

const HALF_SECOND = 2000;
const HALF_MINUTE = 30000;
const CONTAINER_ENV = "container";

export async function connectRabbitMq() {
    const env = process.env.NODE_ENV;
    if (CONTAINER_ENV === env) {
        console.info("Waiting for RabbitMQv to start...");
        setInterval(() => {
            connectRabbitMqAndCreateQueues();
        }, HALF_MINUTE);
    } else {
        connectRabbitMqAndCreateQueues();
    }
}

function connectRabbitMqAndCreateQueues() {
    amqp.connect(RABBIT_MQ_URL, (error, connection) => {
        if (error) {
            throw error;
        }
        createQueue(connection, PRODUCT_STOCK_UPDATE_QUEUE, PRODUCT_STOCK_UPDATE_ROUTING_KEY, PRODUCT_TOPIC);
        createQueue(connection, SALES_CONFIRMATION_QUEUE, SALES_CONFIRMATION_ROUTING_KEY, PRODUCT_TOPIC);
        setTimeout(function () {
            connection.close();
        }, HALF_SECOND);
    })
};

function createQueue(connection, queue, routingKey, topic) {
    connection.createChannel((error, channel) => {
        if (error) { throw error; }
        channel.assertExchange(topic, "topic", { durable: true });
        channel.assertQueue(queue, { durable: true });
        channel.bindQueue(queue, topic, routingKey);
    });
    listenToSalesConfirmationQueue();
}