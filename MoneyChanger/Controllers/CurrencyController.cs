using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using MoneyChanger.Models;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace MoneyChanger.Controllers
{
    [Route("api/currency")]
    [ApiController]
    public class CurrencyController : ControllerBase
    {
        private readonly MoneyChangerDbContext dbc;
        private readonly IConfiguration conf;

        public CurrencyController(MoneyChangerDbContext dbc, IConfiguration conf)
        {
            this.dbc = dbc;
            this.conf = conf;
        }

        [HttpGet]
        public IActionResult GetAll()
        {
            var data = dbc.Currencies.ToList();
            return Ok(data.Select(x => new
            {
                id = x.Id,
                country = x.Country,
                name = x.Name,
                abbreviation = x.Abbreviation
            }).ToList());
        }
        [HttpGet("{id}")]
        public IActionResult GetDetail(int id)
        {
            var data = dbc.Currencies.Find(id);
            if (data == null) return NotFound("Currency not found");
            return Ok(new
            {
                id = data.Id,
                country = data.Country,
                name = data.Name,
                abbreviation = data.Abbreviation
            });
        }

        [HttpGet("usd-rate/{id}")]
        public IActionResult GetUSDRate(int id)
        {
            var data = dbc.UsdRates.FirstOrDefault(x => x.CurrencyId == id);
            if (data == null) return NotFound("The currency's USD rate not found");
            return Ok(new
            {
                id = data.Id,
                currencyId = data.CurrencyId,
                rate = data.Rate,
            });
        }

        [HttpGet("orders")]
        public IActionResult GetOrders()
        {
            var data = dbc.Orders.Include(x => x.OriginCurrency).Include(x => x.TargetCurrency).ToList();
            return Ok(data.Select(x => new
            {
                id = x.Id,
                code = x.Code,
                originCurrency = x.OriginCurrency.Name,
                targetCurrency = x.TargetCurrency.Name,
                rate = x.ConversionRate,
                originNominal = x.OriginNominal,
                targetNominal = x.TargetNominal,
                date = x.OrderDate,
            }).ToList());
        }

        //[HttpGet("exchange-rate/{originCurrencyId}/{targetCurrencyId}/{amount}")]
        //public IActionResult CalculateExchange(int originCurrencyId, int targetCurrencyId, decimal amount)
        //{
        //    if (amount < 1) return BadRequest("Amount must larger than 0");
        //    var currency1 = dbc.Currencies.FirstOrDefault(x => x.Id == originCurrencyId);
        //    if (currency1 == null) return NotFound("Origin currency is not found");
        //    var currency2 = dbc.Currencies.FirstOrDefault(x => x.Id == targetCurrencyId);
        //    if (currency2 == null) return NotFound("Target currency is not found");

        //    var usdRateCurrencyOrigin = dbc.UsdRates.FirstOrDefault(x => x.CurrencyId == originCurrencyId);
        //    if (usdRateCurrencyOrigin == null) return NotFound("USD Rate's origin currency is not found");

        //    var usdRateCurrencyTarget = dbc.UsdRates.FirstOrDefault(x => x.CurrencyId == targetCurrencyId);
        //    if (usdRateCurrencyTarget == null) return NotFound("USD Rate's origin currency is not found");

        //    decimal conversationRate = Math.Round((decimal)usdRateCurrencyTarget.Rate / (decimal)usdRateCurrencyOrigin.Rate, 10);
        //    var minusRate = conversationRate - (conversationRate * 10 / 100);
        //    var plusRate = conversationRate + (conversationRate * 10 / 100);
        //    Random random = new Random();
        //    var options = new[] { minusRate, plusRate };
        //    int randomInt = random.Next(options.Length);
        //    decimal realRate = (decimal)options[randomInt];

        //    decimal nominalResult = Math.Round(realRate * amount, 3);
        //    //decimal realConvertsationRate = conversationRate 
        //    return Ok(new
        //    {
        //        convertsationRate = conversationRate,
        //        realRate = realRate,
        //        nominalResult = nominalResult
        //    });
        //}
        [HttpGet("exchange-rate/{originCurrencyRate}/{targetCurrencyRate}/{amount}")]
        public IActionResult CalculateExchange(decimal originCurrencyRate, decimal targetCurrencyRate, decimal amount)
        {
            decimal conversationRate = Math.Round(targetCurrencyRate / originCurrencyRate, 10);
            var minusRate = conversationRate - (conversationRate * 10 / 100);
            var plusRate = conversationRate + (conversationRate * 10 / 100);
            Random random = new Random();
            var options = new[] { minusRate, plusRate };
            int randomInt = random.Next(options.Length);
            decimal realRate = (decimal)options[randomInt];

            decimal nominalResult = Math.Round(realRate * amount, 3);
            //decimal realConvertsationRate = conversationRate 
            return Ok(new
            {
                convertsationRate = conversationRate,
                realRate = realRate,
                nominalResult = nominalResult
            });
        }

        [HttpPost("order")]
        public IActionResult Order(OrderDTO input)
        {
            if (input.originNominal < 1) return BadRequest("Origin nominal must larger than 0");
            if (input.convertsationRate < 1) return BadRequest("Convertsation rate must larger than 0");
            if (input.targetNominal < 1) return BadRequest("Target nominal must larger than 0");
            var currency1 = dbc.Currencies.FirstOrDefault(x => x.Id == input.originCurrencyId);
            if (currency1 == null) return NotFound("Origin currency is not found");
            var currency2 = dbc.Currencies.FirstOrDefault(x => x.Id == input.targetCurrencyId);
            if (currency2 == null) return NotFound("Target currency is not found");

            dbc.Orders.Add(new Order()
            {
                Code = generateCode(),
                OriginCurrencyId = currency1.Id,
                TargetCurrencyId = currency2.Id,
                ConversionRate= input.convertsationRate,
                OriginNominal= input.originNominal,
                TargetNominal= input.targetNominal,
                OrderDate= DateTime.Now,
            });

            dbc.SaveChanges();
            return Ok("Successfully ordered.");
        }

        private string generateCode()
        {
            const string chars = "ABCDEGHIJKLMNOPQRSTUVEWXYZ1234567890";
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for(int i =0; i < 8; i++)
            {
                sb.Append(chars[random.Next(chars.Length)]);
            }

            return sb.ToString();
        }
    }

    public class ExchangeRateDTO
    {
        [Required] public int currencyId1 { get; set; }
        [Required] public int currencyId2 { get; set; }
        [Required] public int amount { get; set; }
    }

    public class OrderDTO
    {
        [Required] public int originCurrencyId { get; set; }
        [Required] public int targetCurrencyId { get; set; }
        [Required] public decimal convertsationRate { get; set; }
        [Required] public decimal originNominal { get; set; }
        [Required] public decimal targetNominal { get; set; }
    }
}
