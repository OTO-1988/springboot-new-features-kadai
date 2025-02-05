const stripe = Stripe('pk_test_51QjEYeHlBiQVWTKDTYTvoDatfUKHZwsAD8J4ddyLVpeJ1M638JdobP4KhF5HNKNY5DLiRrxEPVe0SMZkvPwx4CCa00cPt43Dhy');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
 stripe.redirectToCheckout({
 	sessionId: sessionId
 })
});