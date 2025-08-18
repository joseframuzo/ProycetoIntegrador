function isCedulaEcuador(val) {
  if (!/^[0-9]{10}$/.test(val)) return false;
  const digits = val.split('').map(Number);
  const coef = [2,1,2,1,2,1,2,1,2];
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    let p = digits[i] * coef[i];
    if (p >= 10) p -= 9;
    sum += p;
  }
  const check = (10 - (sum % 10)) % 10;
  return check === digits[9];
}
module.exports = { isCedulaEcuador };