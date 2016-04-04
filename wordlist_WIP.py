
dolch_list = '''
apple, baby, back, ball, bear, bed, bell, bird, birthday, boat, box, boy,
bread, brother, cake, car, cat, chair, chicken, children, Christmas, coat,corn,
cow, day, dog, doll, door, duck, egg, eye, farm, farmer, father, feet, fire,
fish, floor, flower, game, garden, girl, good-bye, grass, ground, hand, head,
hill, home, horse, house, kitty, leg, letter, man, men, milk, money, morning,
mother, name, nest, night, paper, party, picture, pig, rabbit, rain, ring,
robin, Santa Claus, school, seed, sheep, shoe, sister, snow, song, squirrel,
stick, street, sun, table, thing, time, top, toy, tree, watch, water, way,
wind,window, wood'''

# list of commone nouns by grade
# from http://www.momswhothink.com/reading/list-of-nouns.html
bygrade_list = '''
ball
bat
bed
book
boy
bun
can
cake
cap
car
cat
cow
cub
cup
dad
day
dog
doll
dust
fan
feet
girl
gun
hall
hat
hen
jar
kite
man
map
men
mom
pan
pet
pie
pig
pot
rat
son
sun
toe
tub
van
apple
arm
banana
bike
bird
book
chin
clam
class
clover
club
corn
crayon
crow
crown
crowd
crib
desk
dime
dirt
dress
fang
field
flag
flower
fog
game
heat
hill
home
horn
hose
joke
juice
kite
lake
maid
mask
mice
milk
mint
meal
meat
moon
mother
morning
name
nest
nose
pear
pen
pencil
plant
rain
river
road
rock
room
rose
seed
shape
shoe
shop
show
sink
snail
snake
snow
soda
sofa
star
step
stew
stove
straw
string
summer
swing
table
tank
team
tent
test
toes
tree
vest
water
wing
winter
woman
women
alarm
animal
aunt
bait
balloon
bath
bead
beam
bean
bedroom
boot
bread
brick
brother
camp
chicken
children
crook
deer
dock
doctor
downtown
drum
dust
eye
family
father
fight
flesh
food
frog
goose
grade
grandfather
grandmother
grape
grass
hook
horse
jail
jam
kiss
kitten
light
loaf
lock
lunch
lunchroom
meal
mother
notebook
owl
pail
parent
park
plot
rabbit
rake
robin
sack
sail
scale
sea
sister
soap
song
spark
space
spoon
spot
spy
summer
tiger
toad
town
trail
tramp
tray
trick
trip
uncle
vase
winter
water
week
wheel
wish
wool
yard
zebra
actor
airplane
airport
army
baseball
beef
birthday
boy
brush
bushes
butter
cast
cave
cent
cherries
cherry
cobweb
coil
cracker
dinner
eggnog
elbow
face
fireman
flavor
gate
glove
glue
goldfish
goose
grain
hair
haircut
hobbies
holiday
hot
jellyfish
ladybug
mailbox
number
oatmeal
pail
pancake
pear
pest
popcorn
queen
quicksand
quiet
quilt
rainstorm
scarecrow
scarf
stream
street
sugar
throne
toothpaste
twig
volleyball
wood
wrench
advice
anger
answer
apple
arithmetic
badge
basket
basketball
battle
beast
beetle
beggar
brain
branch
bubble
bucket
cactus
cannon
cattle
celery
cellar
cloth
coach
coast
crate
cream
daughter
donkey
drug
earthquake
feast
fifth
finger
flock
frame
furniture
geese
ghost
giraffe
governor
honey
hope
hydrant
icicle
income
island
jeans
judge
lace
lamp
lettuce
marble
month
north
ocean
patch
plane
playground
poison
riddle
rifle
scale
seashore
sheet
sidewalk
skate
slave
sleet
smoke
stage
station
thrill
throat
throne
title
toothbrush
turkey
underwear
vacation
vegetable
visitor
voyage
year
able
achieve
acoustics
action
activity
aftermath
afternoon
afterthought
apparel
appliance
beginner
believe
bomb
border
boundary
breakfast
cabbage
cable
calculator
calendar
caption
carpenter
cemetery
channel
circle
creator
creature
education
faucet
feather
friction
fruit
fuel
galley
guide
guitar
health
heart
idea
kitten
laborer
language
lawyer
linen
locket
lumber
magic
minister
mitten
money
mountain
music
partner
passenger
pickle
picture
plantation
plastic
pleasure
pocket
police
pollution
railway
recess
reward
route
scene
scent
squirrel
stranger
suit
sweater
temper
territory
texture
thread
treatment
veil
vein
volcano
wealth
weather
wilderness
wren
wrist
writer'''

# init empty list
wlist = []

# dolch_list # separate by comma, trim, lower
wlist.extend(dolch_list.split(','))

# bygrade_list # separate by newline, trim, lower
wlist.extend(bygrade_list.split('\n'))

# trim lower all items in wlist
wlist = [x.strip().lower() for x in wlist]

# remove duplicates
wlist = list(set(wlist))

with open("wlist.txt","w") as fd:
    for itm in wlist:
        astr ="<item>%s</item>" % (itm)
        print(astr)
        print(astr, file=fd)

print(len(wlist))